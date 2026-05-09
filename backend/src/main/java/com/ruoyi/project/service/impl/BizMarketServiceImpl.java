package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.domain.entity.BizMarketItem;
import com.ruoyi.project.domain.entity.BizTransaction;
import com.ruoyi.project.domain.entity.BizUserAsset;
import com.ruoyi.project.domain.vo.MarketItemVO;
import com.ruoyi.project.mapper.BizMarketItemMapper;
import com.ruoyi.project.mapper.BizTransactionMapper;
import com.ruoyi.project.mapper.BizUserAssetMapper;
import com.ruoyi.project.service.IBizMarketService;
import com.ruoyi.project.service.IBizTransactionService;
import com.ruoyi.project.service.IBizUserPointsLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 市场服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizMarketServiceImpl extends ServiceImpl<BizMarketItemMapper, BizMarketItem> implements IBizMarketService {

    private final BizUserAssetMapper userAssetMapper;
    private final BizTransactionMapper transactionMapper;
    private final IBizTransactionService transactionService;
    private final IBizUserPointsLogService pointsLogService;

    @Override
    public Page<MarketItemVO> pageItems(long pageNum, long pageSize, String itemName, String itemType,
                                        String rarity, Integer minPrice, Integer maxPrice, String sortBy) {
        Page<BizMarketItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizMarketItem> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(BizMarketItem::getStatus, 0);

        if (itemName != null && !itemName.isBlank()) {
            wrapper.like(BizMarketItem::getItemName, itemName);
        }
        if (itemType != null && !itemType.isBlank()) {
            wrapper.eq(BizMarketItem::getItemType, itemType);
        }
        if (rarity != null && !rarity.isBlank()) {
            wrapper.eq(BizMarketItem::getItemRarity, rarity);
        }
        if (minPrice != null) {
            wrapper.ge(BizMarketItem::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(BizMarketItem::getPrice, maxPrice);
        }

        if ("price_asc".equals(sortBy)) {
            wrapper.orderByAsc(BizMarketItem::getPrice);
        } else if ("price_desc".equals(sortBy)) {
            wrapper.orderByDesc(BizMarketItem::getPrice);
        } else {
            wrapper.orderByDesc(BizMarketItem::getCreateTime);
        }

        Page<BizMarketItem> result = this.page(page, wrapper);

        Page<MarketItemVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<MarketItemVO> voList = new ArrayList<>();

        for (BizMarketItem item : result.getRecords()) {
            MarketItemVO vo = convertToVO(item);
            voList.add(vo);
        }

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public MarketItemVO getItemById(Long id) {
        BizMarketItem item = this.getById(id);
        if (item == null) {
            return null;
        }
        return convertToVO(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long listAsset(Long userId, Long assetId, Integer price) {
        BizUserAsset asset = userAssetMapper.selectById(assetId);
        if (asset == null) {
            throw new RuntimeException("资产不存在");
        }

        if (!userId.equals(asset.getUserId())) {
            throw new RuntimeException("无权操作此资产");
        }

        if (asset.getStatus() != 1) {
            throw new RuntimeException("资产状态不可上架");
        }

        asset.setStatus(2);
        asset.setUpdateTime(LocalDateTime.now());
        userAssetMapper.updateById(asset);

        BizMarketItem marketItem = new BizMarketItem();
        marketItem.setAssetId(assetId);
        marketItem.setItemId(asset.getItemId());
        marketItem.setItemName(asset.getItemName());
        marketItem.setItemImage(asset.getItemImage());
        marketItem.setItemRarity(asset.getItemRarity());
        marketItem.setItemType(asset.getItemType());
        marketItem.setSellerId(userId);
        marketItem.setPrice(price);
        marketItem.setStatus(0);
        marketItem.setCreateTime(LocalDateTime.now());
        marketItem.setUpdateTime(LocalDateTime.now());

        this.save(marketItem);
        return marketItem.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String buyItem(Long buyerId, Long itemId) {
        BizMarketItem marketItem = this.getById(itemId);
        if (marketItem == null) {
            throw new RuntimeException("商品不存在");
        }

        if (marketItem.getStatus() != 0) {
            throw new RuntimeException("商品已下架或已售出");
        }

        if (buyerId.equals(marketItem.getSellerId())) {
            throw new RuntimeException("不能购买自己上架的商品");
        }

        int fee = (int) Math.ceil(marketItem.getPrice() * 0.01);
        int sellerAmount = marketItem.getPrice() - fee;

        String orderId = generateOrderId();

        // 扣减买家积分
        boolean buyerDeducted = pointsLogService.deductPoints(buyerId, marketItem.getPrice(), "MARKET_BUY", orderId);
        if (!buyerDeducted) {
            throw new RuntimeException("积分不足，购买失败");
        }

        // 给卖家加积分（扣除手续费后）
        pointsLogService.addPoints(marketItem.getSellerId(), sellerAmount, "MARKET_SELL", orderId);

        BizTransaction transaction = new BizTransaction();
        transaction.setOrderId(orderId);
        transaction.setBuyerId(buyerId);
        transaction.setSellerId(marketItem.getSellerId());
        transaction.setAssetId(marketItem.getAssetId());
        transaction.setItemId(marketItem.getItemId());
        transaction.setItemName(marketItem.getItemName());
        transaction.setItemImage(marketItem.getItemImage());
        transaction.setItemRarity(marketItem.getItemRarity());
        transaction.setAmount(marketItem.getPrice());
        transaction.setFee(fee);
        transaction.setSellerAmount(sellerAmount);
        transaction.setStatus(0);
        transaction.setCreateTime(LocalDateTime.now());
        transaction.setUpdateTime(LocalDateTime.now());

        transactionMapper.insert(transaction);

        marketItem.setStatus(1);
        marketItem.setOrderId(orderId);
        marketItem.setSoldTime(LocalDateTime.now());
        marketItem.setUpdateTime(LocalDateTime.now());
        this.updateById(marketItem);

        BizUserAsset asset = userAssetMapper.selectById(marketItem.getAssetId());
        if (asset != null) {
            asset.setUserId(buyerId);
            asset.setStatus(1);
            asset.setUpdateTime(LocalDateTime.now());
            userAssetMapper.updateById(asset);
        }

        return orderId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delistAsset(Long itemId, Long userId) {
        BizMarketItem marketItem = this.getById(itemId);
        if (marketItem == null) {
            throw new RuntimeException("商品不存在");
        }

        if (!userId.equals(marketItem.getSellerId())) {
            throw new RuntimeException("无权操作此商品");
        }

        if (marketItem.getStatus() != 0) {
            throw new RuntimeException("商品已售出，无法下架");
        }

        marketItem.setStatus(2);
        marketItem.setDelistTime(LocalDateTime.now());
        marketItem.setUpdateTime(LocalDateTime.now());
        this.updateById(marketItem);

        BizUserAsset asset = userAssetMapper.selectById(marketItem.getAssetId());
        if (asset != null) {
            asset.setStatus(1);
            asset.setUpdateTime(LocalDateTime.now());
            userAssetMapper.updateById(asset);
        }

        return true;
    }

    @Override
    public List<MarketItemVO> getUserListings(Long userId) {
        LambdaQueryWrapper<BizMarketItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizMarketItem::getSellerId, userId);
        wrapper.orderByDesc(BizMarketItem::getCreateTime);

        List<BizMarketItem> items = this.list(wrapper);
        List<MarketItemVO> voList = new ArrayList<>();

        for (BizMarketItem item : items) {
            voList.add(convertToVO(item));
        }

        return voList;
    }

    private MarketItemVO convertToVO(BizMarketItem item) {
        MarketItemVO vo = new MarketItemVO();
        vo.setId(item.getId());
        vo.setAssetId(item.getAssetId());
        vo.setItemId(item.getItemId());
        vo.setItemName(item.getItemName());
        vo.setItemImage(item.getItemImage());
        vo.setItemRarity(item.getItemRarity());
        vo.setItemType(item.getItemType());
        vo.setSellerId(item.getSellerId());
        vo.setPrice(item.getPrice());
        vo.setStatus(item.getStatus());
        vo.setCreateTime(item.getCreateTime() != null ?
                item.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        return vo;
    }

    private String generateOrderId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(10000, 99999);
        return "ORD" + timestamp + random;
    }
}