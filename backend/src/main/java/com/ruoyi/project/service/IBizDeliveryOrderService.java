package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizDeliveryOrder;

import java.util.Map;

public interface IBizDeliveryOrderService extends IService<BizDeliveryOrder> {

    /**
     * 分页查询核销订单列表
     */
    Page<BizDeliveryOrder> pageOrders(long pageNum, long pageSize, String orderId, Integer status);

    /**
     * 获取统计数据
     */
    Map<String, Object> getStats();

    /**
     * 根据订单号获取订单
     */
    BizDeliveryOrder getByOrderId(String orderId);

    /**
     * 创建核销订单
     */
    String createOrder(Long userId, Long assetId, Long itemId, String itemName,
                      String itemImage, String itemRarity, String receiver,
                      String phone, String address);

    /**
     * 发货
     */
    boolean ship(String orderId, String expressCompany, String expressNo, String remark);

    /**
     * 确认收货
     */
    boolean complete(String orderId);

    /**
     * 取消订单
     */
    boolean cancel(String orderId);
}