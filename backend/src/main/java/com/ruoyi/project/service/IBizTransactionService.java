package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizTransaction;

import java.util.Map;

public interface IBizTransactionService extends IService<BizTransaction> {

    /**
     * 分页查询交易订单列表
     */
    Page<BizTransaction> pageTransactions(long pageNum, long pageSize, String orderId, Integer status);

    /**
     * 获取今日统计数据
     */
    Map<String, Object> getTodayStats();

    /**
     * 根据订单号获取订单
     */
    BizTransaction getByOrderId(String orderId);

    /**
     * 创建交易订单
     */
    String createTransaction(Long buyerId, Long sellerId, Long assetId, Long itemId, String itemName,
                             String itemImage, String itemRarity, Integer amount);

    /**
     * 处理交易成功回调
     */
    boolean handleTransactionSuccess(String orderId, String rocketmqTxId);

    /**
     * 处理交易失败回调
     */
    boolean handleTransactionFailed(String orderId, String errorMsg);

    /**
     * 人工补偿处理
     */
    boolean compensate(String orderId);

    /**
     * 重试处理
     */
    boolean retry(String orderId);

    /**
     * 检查交易是否已完成（幂等性校验）
     */
    boolean isTransactionCompleted(Long orderId);
}