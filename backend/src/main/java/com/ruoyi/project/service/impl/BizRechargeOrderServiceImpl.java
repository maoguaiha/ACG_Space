package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.domain.entity.BizRechargeOrder;
import com.ruoyi.project.mapper.BizRechargeOrderMapper;
import com.ruoyi.project.service.IBizRechargeOrderService;
import com.ruoyi.project.service.IBizUserPointsLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizRechargeOrderServiceImpl extends ServiceImpl<BizRechargeOrderMapper, BizRechargeOrder> implements IBizRechargeOrderService {

    private final IBizUserPointsLogService pointsLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizRechargeOrder createOrder(Long userId, BigDecimal amount, int points) {
        BizRechargeOrder order = new BizRechargeOrder();
        order.setOrderNo("RCH" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setUserId(userId);
        order.setAmount(amount);
        order.setPoints(points);
        order.setPayStatus(0);
        order.setPayType("mock");
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setDelFlag(0);
        save(order);
        return order;
    }

    @Override
    public BizRechargeOrder getByOrderNo(String orderNo) {
        return getOne(new LambdaQueryWrapper<BizRechargeOrder>()
                .eq(BizRechargeOrder::getOrderNo, orderNo)
                .eq(BizRechargeOrder::getDelFlag, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean mockPaySuccess(String orderNo) {
        BizRechargeOrder order = getByOrderNo(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getPayStatus() != 0) {
            throw new RuntimeException("订单状态异常");
        }

        order.setPayStatus(1);
        order.setPayTime(LocalDateTime.now());
        order.setTradeNo("MOCK_" + System.currentTimeMillis());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);

        pointsLogService.addPoints(order.getUserId(), order.getPoints(), "RECHARGE", orderNo);

        log.info("充值成功: userId={}, amount={}, points={}", order.getUserId(), order.getAmount(), order.getPoints());
        return true;
    }

    @Override
    public Page<BizRechargeOrder> pageOrders(Long userId, long pageNum, long pageSize) {
        Page<BizRechargeOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizRechargeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizRechargeOrder::getDelFlag, 0);
        if (userId != null) {
            wrapper.eq(BizRechargeOrder::getUserId, userId);
        }
        wrapper.orderByDesc(BizRechargeOrder::getCreateTime);
        return page(page, wrapper);
    }
}
