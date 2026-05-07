package com.ruoyi.project.mq.consumer;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.project.common.constant.MqConstants;
import com.ruoyi.project.domain.dto.TransactionEventDTO;
import com.ruoyi.project.service.IBizTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 交易事件消息消费者
 * 处理交易成功/失败的事件
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = MqConstants.TOPIC_TRANSACTION,
        consumerGroup = MqConstants.CONSUMER_GROUP_TRANSACTION
)
public class TransactionConsumer implements RocketMQListener<String> {

    private final IBizTransactionService transactionService;

    @Override
    public void onMessage(String message) {
        log.info("RocketMQ 接收到交易事件消息: {}", message);

        try {
            TransactionEventDTO event = JSON.parseObject(message, TransactionEventDTO.class);
            if (event.getOrderId() == null) {
                log.error("消息格式错误，缺少订单号: {}", message);
                return;
            }

            // 幂等性校验：检查是否已处理过该订单
            Long orderId = parseOrderId(event.getOrderId());
            if (orderId == null) {
                log.error("订单号格式错误: {}", event.getOrderId());
                return;
            }
            if (isOrderAlreadyProcessed(orderId)) {
                log.info("订单已处理，跳过重复消息: {}", orderId);
                return;
            }

            log.info("处理交易事件，订单号: {}, 类型: {}",
                    event.getOrderId(), event.getEventType());

            if (MqConstants.TAG_ASSET_TRANSFER.equals(event.getEventType())) {
                handleAssetTransfer(event);
            } else if (MqConstants.TAG_POINTS_TRANSFER.equals(event.getEventType())) {
                handlePointsTransfer(event);
            } else {
                log.warn("未知的交易事件类型: {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("消费交易事件消息失败, message: {}", message, e);
            throw new RuntimeException("消费失败，触发重试", e);
        }
    }

    /**
     * 解析订单号字符串为 Long
     */
    private Long parseOrderId(String orderIdStr) {
        if (orderIdStr == null || orderIdStr.isEmpty()) {
            return null;
        }
        // 移除可能的前缀（如 "TXN"）
        String numericPart = orderIdStr.replaceAll("[^0-9]", "");
        if (numericPart.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(numericPart);
        } catch (NumberFormatException e) {
            log.warn("订单号解析失败: {}", orderIdStr);
            return null;
        }
    }

    /**
     * 幂等性校验：检查订单是否已处理
     */
    private boolean isOrderAlreadyProcessed(Long orderId) {
        try {
            return transactionService.isTransactionCompleted(orderId);
        } catch (Exception e) {
            log.warn("幂等性校验异常，继续处理消息: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 处理资产转移
     */
    private void handleAssetTransfer(TransactionEventDTO event) {
        try {
            boolean success = transactionService.handleTransactionSuccess(
                    event.getOrderId(),
                    null
            );
            if (success) {
                log.info("资产转移成功，订单号: {}", event.getOrderId());
            } else {
                log.error("资产转移失败，订单号: {}", event.getOrderId());
            }
        } catch (Exception e) {
            log.error("处理资产转移异常，订单号: {}", event.getOrderId(), e);
            throw e;
        }
    }

    /**
     * 处理积分转移
     */
    private void handlePointsTransfer(TransactionEventDTO event) {
        log.info("积分转移已通过本地事务处理，订单号: {}, 卖家: {} 获得 {} 积分",
                event.getOrderId(), event.getSellerId(), event.getSellerAmount());
    }
}