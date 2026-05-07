package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.domain.entity.BizDeliveryOrder;
import com.ruoyi.project.domain.entity.BizUserAsset;
import com.ruoyi.project.mapper.BizDeliveryOrderMapper;
import com.ruoyi.project.mapper.BizUserAssetMapper;
import com.ruoyi.project.service.IBizDeliveryOrderService;
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
public class BizDeliveryOrderServiceImpl extends ServiceImpl<BizDeliveryOrderMapper, BizDeliveryOrder> implements IBizDeliveryOrderService {

    private final BizUserAssetMapper userAssetMapper;

    @Override
    public Page<BizDeliveryOrder> pageOrders(long pageNum, long pageSize, String orderId, Integer status) {
        Page<BizDeliveryOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizDeliveryOrder> wrapper = new LambdaQueryWrapper<>();

        if (orderId != null && !orderId.isBlank()) {
            wrapper.eq(BizDeliveryOrder::getOrderId, orderId);
        }
        if (status != null) {
            wrapper.eq(BizDeliveryOrder::getStatus, status);
        }

        wrapper.orderByDesc(BizDeliveryOrder::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Map<String, Object> getStats() {
        LambdaQueryWrapper<BizDeliveryOrder> wrapper = new LambdaQueryWrapper<>();

        long pending = this.count(wrapper.clone().eq(BizDeliveryOrder::getStatus, 0));
        long shipped = this.count(wrapper.clone().eq(BizDeliveryOrder::getStatus, 1));
        long completed = this.count(wrapper.clone().eq(BizDeliveryOrder::getStatus, 2));

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long monthlyRedeem = this.count(wrapper.clone()
                .eq(BizDeliveryOrder::getStatus, 2)
                .ge(BizDeliveryOrder::getCompleteTime, startOfMonth));

        Map<String, Object> stats = new HashMap<>();
        stats.put("pending", pending);
        stats.put("shipped", shipped);
        stats.put("completed", completed);
        stats.put("monthlyRedeem", monthlyRedeem);

        return stats;
    }

    @Override
    public BizDeliveryOrder getByOrderId(String orderId) {
        return this.getOne(new LambdaQueryWrapper<BizDeliveryOrder>()
                .eq(BizDeliveryOrder::getOrderId, orderId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, Long assetId, Long itemId, String itemName,
                             String itemImage, String itemRarity, String receiver,
                             String phone, String address) {
        String orderId = generateOrderId();

        BizDeliveryOrder order = new BizDeliveryOrder();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setAssetId(assetId);
        order.setItemId(itemId);
        order.setItemName(itemName);
        order.setItemImage(itemImage);
        order.setItemRarity(itemRarity);
        order.setReceiver(receiver);
        order.setPhone(phone);
        order.setAddress(address);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        this.save(order);

        return orderId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean ship(String orderId, String expressCompany, String expressNo, String remark) {
        BizDeliveryOrder order = this.getByOrderId(orderId);
        if (order == null || order.getStatus() != 0) {
            log.warn("发货失败，订单不存在或状态不允许: {}", orderId);
            return false;
        }

        order.setExpressCompany(expressCompany);
        order.setExpressNo(expressNo);
        if (remark != null) {
            order.setRemark(remark);
        }
        order.setStatus(1);
        order.setShipTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        return this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean complete(String orderId) {
        BizDeliveryOrder order = this.getByOrderId(orderId);
        if (order == null || order.getStatus() != 1) {
            log.warn("确认收货失败，订单不存在或状态不允许: {}", orderId);
            return false;
        }

        order.setStatus(2);
        order.setCompleteTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        BizUserAsset asset = userAssetMapper.selectById(order.getAssetId());
        if (asset != null) {
            asset.setIsCertified(1);
            asset.setCertifiedTime(LocalDateTime.now());
            asset.setUpdateTime(LocalDateTime.now());
            userAssetMapper.updateById(asset);
        }

        return this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(String orderId) {
        BizDeliveryOrder order = this.getByOrderId(orderId);
        if (order == null || (order.getStatus() != 0 && order.getStatus() != 1)) {
            log.warn("取消订单失败，订单不存在或状态不允许: {}", orderId);
            return false;
        }

        order.setStatus(3);
        order.setUpdateTime(LocalDateTime.now());

        return this.updateById(order);
    }

    private String generateOrderId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(10000, 99999);
        return "DLV" + timestamp + random;
    }
}