package com.ruoyi.project.mq.listener;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.project.domain.dto.TransactionEventDTO;
import com.ruoyi.project.domain.entity.BizTransactionLog;
import com.ruoyi.project.domain.entity.BizUserAsset;
import com.ruoyi.project.service.IBizTransactionLogService;
import com.ruoyi.project.service.IBizTransactionService;
import com.ruoyi.project.service.IBizUserAssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * RocketMQ事务消息本地事务监听器
 * 负责执行本地事务和处理事务回查
 */
@Slf4j
@Component
@RocketMQTransactionListener
@RequiredArgsConstructor
public class TransactionLocalListener implements RocketMQLocalTransactionListener {

    private final IBizTransactionService transactionService;
    private final IBizTransactionLogService transactionLogService;
    private final IBizUserAssetService assetService;

    /**
     * 执行本地事务
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        try {
            TransactionEventDTO event = (TransactionEventDTO) arg;
            String transactionId = (String) msg.getHeaders().get("rocketmq_TRANSACTION_ID");
            
            log.info("执行本地事务, transactionId: {}, orderId: {}, eventType: {}",
                    transactionId, event.getOrderId(), event.getEventType());

            // 根据事件类型执行不同的本地事务
            switch (event.getEventType()) {
                case "asset_transfer":
                    return executeAssetTransfer(event, transactionId);
                case "points_transfer":
                    return executePointsTransfer(event, transactionId);
                default:
                    log.warn("未知事件类型: {}", event.getEventType());
                    return RocketMQLocalTransactionState.ROLLBACK;
            }
        } catch (Exception e) {
            log.error("执行本地事务异常", e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 执行资产转移本地事务
     */
    private RocketMQLocalTransactionState executeAssetTransfer(TransactionEventDTO event, String transactionId) {
        try {
            // 检查买家资产是否存在
            BizUserAsset buyerAsset = assetService.getById(event.getAssetId());
            if (buyerAsset == null || buyerAsset.getDelFlag() != 0) {
                log.error("买家资产不存在, assetId: {}", event.getAssetId());
                return RocketMQLocalTransactionState.ROLLBACK;
            }

            // 执行资产转移（标记为已卖出）
            buyerAsset.setStatus(2); // 已卖出
            assetService.updateById(buyerAsset);

            // 更新事务日志状态为提交
            transactionLogService.markCommitted(transactionId);
            
            log.info("资产转移本地事务执行成功, transactionId: {}, orderId: {}", transactionId, event.getOrderId());
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            log.error("执行资产转移本地事务异常, transactionId: {}", transactionId, e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 执行积分转移本地事务
     */
    private RocketMQLocalTransactionState executePointsTransfer(TransactionEventDTO event, String transactionId) {
        try {
            // 检查订单状态
            if (transactionService.getByOrderId(event.getOrderId()) == null) {
                log.error("订单不存在, orderId: {}", event.getOrderId());
                return RocketMQLocalTransactionState.ROLLBACK;
            }

            // 更新事务日志状态为提交
            transactionLogService.markCommitted(transactionId);
            
            log.info("积分转移本地事务执行成功, transactionId: {}, orderId: {}", transactionId, event.getOrderId());
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            log.error("执行积分转移本地事务异常, transactionId: {}", transactionId, e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 事务回查
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        String transactionId = (String) msg.getHeaders().get("rocketmq_TRANSACTION_ID");
        
        log.info("事务回查, transactionId: {}", transactionId);

        try {
            BizTransactionLog logEntry = transactionLogService.getByTransactionId(transactionId);
            
            if (logEntry == null) {
                log.warn("事务日志不存在, transactionId: {}", transactionId);
                return RocketMQLocalTransactionState.ROLLBACK;
            }

            // 更新回查次数
            transactionLogService.incrementCheckCount(transactionId);

            // 根据日志状态决定处理方式
            switch (logEntry.getStatus()) {
                case 0: // 准备中
                    log.warn("事务仍在准备中, transactionId: {}, checkCount: {}", 
                            transactionId, logEntry.getCheckCount());
                    // 超过3次回查仍未确认，回滚
                    if (logEntry.getCheckCount() >= 3) {
                        transactionLogService.markRolledBack(transactionId);
                        return RocketMQLocalTransactionState.ROLLBACK;
                    }
                    return RocketMQLocalTransactionState.UNKNOWN;
                    
                case 1: // 已提交
                    log.info("事务已提交, transactionId: {}", transactionId);
                    return RocketMQLocalTransactionState.COMMIT;
                    
                case 2: // 已回滚
                    log.info("事务已回滚, transactionId: {}", transactionId);
                    return RocketMQLocalTransactionState.ROLLBACK;
                    
                default:
                    log.warn("未知事务状态: {}, transactionId: {}", logEntry.getStatus(), transactionId);
                    return RocketMQLocalTransactionState.ROLLBACK;
            }
        } catch (Exception e) {
            log.error("事务回查异常, transactionId: {}", transactionId, e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }
}