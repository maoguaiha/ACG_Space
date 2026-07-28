package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.domain.entity.BizRedeemOrder;
import com.ruoyi.project.domain.entity.BizMessage;
import com.ruoyi.project.domain.entity.BizUserAsset;
import com.ruoyi.project.mapper.BizMessageMapper;
import com.ruoyi.project.mapper.BizRedeemOrderMapper;
import com.ruoyi.project.mapper.BizUserAssetMapper;
import com.ruoyi.project.service.IBizRedeemOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizRedeemOrderServiceImpl extends ServiceImpl<BizRedeemOrderMapper, BizRedeemOrder> implements IBizRedeemOrderService {

    private final BizUserAssetMapper userAssetMapper;
    private final BizMessageMapper messageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizRedeemOrder createOrder(Long userId, Long assetId, String receiver, String phone, String province, String city, String district, String address) {
        BizUserAsset asset = userAssetMapper.selectById(assetId);
        if (asset == null) {
            throw new RuntimeException("资产不存在");
        }
        if (!userId.equals(asset.getUserId())) {
            throw new RuntimeException("无权操作此资产");
        }
        if (asset.getStatus() != 1) {
            throw new RuntimeException("资产状态异常");
        }
        if (asset.getIsPhysical() == null || asset.getIsPhysical() != 1) {
            throw new RuntimeException("此物品不可兑换实物");
        }

        BizRedeemOrder order = new BizRedeemOrder();
        order.setOrderNo("RDM" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setUserId(userId);
        order.setAssetId(assetId);
        order.setItemId(asset.getItemId());
        order.setItemName(asset.getItemName());
        order.setItemImage(asset.getItemImage());
        order.setItemRarity(asset.getItemRarity());
        order.setReceiver(receiver);
        order.setPhone(phone);
        order.setProvince(province);
        order.setCity(city);
        order.setDistrict(district);
        order.setAddress(address);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setDelFlag(0);
        save(order);

        asset.setStatus(2);
        asset.setUpdateTime(LocalDateTime.now());
        userAssetMapper.updateById(asset);

        log.info("创建兑换订单: userId={}, assetId={}, orderNo={}", userId, assetId, order.getOrderNo());

        // 发送系统通知
        try {
            BizMessage msg = new BizMessage();
            msg.setFromUserId(1L);
            msg.setToUserId(userId);
            msg.setContent("🎉 您的兑换订单已创建成功！订单号：" + order.getOrderNo()
                    + "，物品：" + (asset.getItemName() != null ? asset.getItemName() : "") + "，请等待发货。");
            msg.setIsRead(false);
            msg.setCreateTime(LocalDateTime.now());
            messageMapper.insert(msg);
        } catch (Exception e) {
            log.warn("发送订单通知失败", e);
        }

        return order;
    }

    @Override
    public Page<BizRedeemOrder> pageUserOrders(Long userId, long pageNum, long pageSize, Integer status) {
        Page<BizRedeemOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizRedeemOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizRedeemOrder::getUserId, userId)
                .eq(BizRedeemOrder::getDelFlag, 0);
        if (status != null) {
            wrapper.eq(BizRedeemOrder::getStatus, status);
        }
        wrapper.orderByDesc(BizRedeemOrder::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public Page<BizRedeemOrder> pageAdminOrders(long pageNum, long pageSize, Integer status) {
        Page<BizRedeemOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizRedeemOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizRedeemOrder::getDelFlag, 0);
        if (status != null) {
            wrapper.eq(BizRedeemOrder::getStatus, status);
        }
        wrapper.orderByDesc(BizRedeemOrder::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLogistics(Long orderId, String company, String trackingNo) {
        BizRedeemOrder order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态异常");
        }

        order.setStatus(1);
        order.setLogisticsCompany(company);
        order.setLogisticsNo(trackingNo);
        order.setShipTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        return updateById(order);
    }
}
