package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizRedeemOrder;

public interface IBizRedeemOrderService extends IService<BizRedeemOrder> {

    BizRedeemOrder createOrder(Long userId, Long assetId, String receiver, String phone, String province, String city, String district, String address);

    Page<BizRedeemOrder> pageUserOrders(Long userId, long pageNum, long pageSize, Integer status);

    Page<BizRedeemOrder> pageAdminOrders(long pageNum, long pageSize, Integer status);

    boolean updateLogistics(Long orderId, String company, String trackingNo);
}
