package com.ruoyi.project.service;

import com.ruoyi.project.domain.entity.BizTransactionLog;

/**
 * RocketMQ事务日志服务
 */
public interface IBizTransactionLogService {

    /**
     * 记录事务日志
     */
    void saveLog(String transactionId, String topic, String tag, String businessType, String businessData);

    /**
     * 更新事务状态为提交
     */
    void markCommitted(String transactionId);

    /**
     * 更新事务状态为回滚
     */
    void markRolledBack(String transactionId);

    /**
     * 获取事务日志
     */
    BizTransactionLog getByTransactionId(String transactionId);

    /**
     * 更新回查次数
     */
    void incrementCheckCount(String transactionId);
}