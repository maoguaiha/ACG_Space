package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.domain.entity.BizTransaction;
import com.ruoyi.project.domain.entity.BizUserAsset;
import com.ruoyi.project.mapper.BizTransactionMapper;
import com.ruoyi.project.mapper.BizUserAssetMapper;
import com.ruoyi.project.service.IBizTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizTransactionServiceImpl extends ServiceImpl<BizTransactionMapper, BizTransaction> implements IBizTransactionService {

    private final BizUserAssetMapper userAssetMapper;

    @Override
    public Page<BizTransaction> pageTransactions(long pageNum, long pageSize, String orderId, Integer status) {
        Page<BizTransaction> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizTransaction> wrapper = new LambdaQueryWrapper<>();

        if (orderId != null && !orderId.isBlank()) {
            wrapper.eq(BizTransaction::getOrderId, orderId);
        }
        if (status != null) {
            wrapper.eq(BizTransaction::getStatus, status);
        }

        wrapper.orderByDesc(BizTransaction::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Map<String, Object> getTodayStats() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();

        LambdaQueryWrapper<BizTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(BizTransaction::getCreateTime, startOfDay)
                .lt(BizTransaction::getCreateTime, endOfDay);

        var todayList = this.list(wrapper);

        int totalAmount = 0;
        int successCount = 0;
        int pendingCount = 0;
        int errorCount = 0;

        for (BizTransaction tx : todayList) {
            totalAmount += tx.getAmount();
            switch (tx.getStatus()) {
                case 1 -> successCount++;
                case 0, 3 -> pendingCount++;
                case 2 -> errorCount++;
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("amount", totalAmount);
        stats.put("successCount", successCount);
        stats.put("pendingCount", pendingCount);
        stats.put("errorCount", errorCount);

        return stats;
    }

    @Override
    public BizTransaction getByOrderId(String orderId) {
        return this.getOne(new LambdaQueryWrapper<BizTransaction>()
                .eq(BizTransaction::getOrderId, orderId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createTransaction(Long buyerId, Long sellerId, Long assetId, Long itemId,
                                    String itemName, String itemImage, String itemRarity, Integer amount) {
        String orderId = generateOrderId();

        int fee = (int) Math.ceil(amount * 0.01);
        int sellerAmount = amount - fee;

        BizTransaction transaction = new BizTransaction();
        transaction.setOrderId(orderId);
        transaction.setBuyerId(buyerId);
        transaction.setSellerId(sellerId);
        transaction.setAssetId(assetId);
        transaction.setItemId(itemId);
        transaction.setItemName(itemName);
        transaction.setItemImage(itemImage);
        transaction.setItemRarity(itemRarity);
        transaction.setAmount(amount);
        transaction.setFee(fee);
        transaction.setSellerAmount(sellerAmount);
        transaction.setStatus(0);
        transaction.setCreateTime(LocalDateTime.now());
        transaction.setUpdateTime(LocalDateTime.now());

        this.save(transaction);

        return orderId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleTransactionSuccess(String orderId, String rocketmqTxId) {
        BizTransaction transaction = this.getByOrderId(orderId);
        if (transaction == null) {
            log.error("事务处理成功回调失败，订单不存在: {}", orderId);
            return false;
        }

        transaction.setStatus(1);
        transaction.setRocketmqTxId(rocketmqTxId);
        transaction.setCompleteTime(LocalDateTime.now());
        transaction.setUpdateTime(LocalDateTime.now());

        BizUserAsset asset = userAssetMapper.selectById(transaction.getAssetId());
        if (asset != null) {
            asset.setUserId(transaction.getBuyerId());
            asset.setStatus(1);
            asset.setUpdateTime(LocalDateTime.now());
            userAssetMapper.updateById(asset);
        }

        return this.updateById(transaction);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleTransactionFailed(String orderId, String errorMsg) {
        BizTransaction transaction = this.getByOrderId(orderId);
        if (transaction == null) {
            log.error("事务处理失败回调失败，订单不存在: {}", orderId);
            return false;
        }

        transaction.setStatus(2);
        transaction.setErrorMsg(errorMsg);
        transaction.setUpdateTime(LocalDateTime.now());

        return this.updateById(transaction);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean compensate(String orderId) {
        BizTransaction transaction = this.getByOrderId(orderId);
        if (transaction == null) {
            return false;
        }

        transaction.setStatus(1);
        transaction.setCompleteTime(LocalDateTime.now());
        transaction.setUpdateTime(LocalDateTime.now());

        BizUserAsset asset = userAssetMapper.selectById(transaction.getAssetId());
        if (asset != null) {
            asset.setUserId(transaction.getBuyerId());
            asset.setStatus(1);
            asset.setUpdateTime(LocalDateTime.now());
            userAssetMapper.updateById(asset);
        }

        return this.updateById(transaction);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean retry(String orderId) {
        BizTransaction transaction = this.getByOrderId(orderId);
        if (transaction == null) {
            return false;
        }

        transaction.setStatus(0);
        transaction.setErrorMsg(null);
        transaction.setUpdateTime(LocalDateTime.now());

        return this.updateById(transaction);
    }

    private String generateOrderId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(10000, 99999);
        return "TXN" + timestamp + random;
    }

    @Override
    public boolean isTransactionCompleted(Long orderId) {
        BizTransaction transaction = this.getById(orderId);
        if (transaction == null) {
            return false;
        }
        // 状态为1表示已完成，状态为2表示已失败
        return transaction.getStatus() == 1 || transaction.getStatus() == 2;
    }
}