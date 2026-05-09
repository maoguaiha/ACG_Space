package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizRechargeOrder;

import java.math.BigDecimal;

public interface IBizRechargeOrderService extends IService<BizRechargeOrder> {

    BizRechargeOrder createOrder(Long userId, BigDecimal amount, int points);

    BizRechargeOrder getByOrderNo(String orderNo);

    boolean mockPaySuccess(String orderNo);

    Page<BizRechargeOrder> pageOrders(Long userId, long pageNum, long pageSize);
}
