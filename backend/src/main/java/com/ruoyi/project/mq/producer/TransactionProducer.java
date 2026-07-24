package com.ruoyi.project.mq.producer;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.project.common.constant.MqConstants;
import com.ruoyi.project.domain.dto.TransactionEventDTO;
import com.ruoyi.project.service.IBizTransactionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * 交易事件消息生产者
 * 用于发送交易相关的 RocketMQ 事务消息
 *
 * 仅在配置了 rocketmq.name-server 时才加载，避免无 MQ 环境下启动崩溃
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rocketmq.name-server", matchIfMissing = false)
public class TransactionProducer {

    private final RocketMQTemplate rocketMQTemplate;
    private final IBizTransactionLogService transactionLogService;

    /**
     * 发送资产转移事务消息
     *
     * @param event 交易事件
     * @return 发送的 transactionId
     */
    public String sendAssetTransferMessage(TransactionEventDTO event) {
        event.setEventType(MqConstants.TAG_ASSET_TRANSFER);
        return sendTransactionMessage(event);
    }

    /**
     * 发送积分转移事务消息
     *
     * @param event 交易事件
     * @return 发送的 transactionId
     */
    public String sendPointsTransferMessage(TransactionEventDTO event) {
        event.setEventType(MqConstants.TAG_POINTS_TRANSFER);
        return sendTransactionMessage(event);
    }

    /**
     * 发送事务消息
     *
     * @param event 交易事件
     * @return 发送的 transactionId
     */
    private String sendTransactionMessage(TransactionEventDTO event) {
        String messageBody = JSON.toJSONString(event);
        log.info("发送交易事务消息，订单号: {}, 类型: {}, 内容: {}",
                event.getOrderId(), event.getEventType(), messageBody);

        Message<String> message = MessageBuilder.withPayload(messageBody)
                .setHeader("orderId", event.getOrderId())
                .setHeader("eventType", event.getEventType())
                .build();

        try {
            org.apache.rocketmq.client.producer.SendResult result =
                    rocketMQTemplate.sendMessageInTransaction(
                            MqConstants.TOPIC_TRANSACTION + ":*",
                            message,
                            event
                    );

            if (result != null && result.getTransactionId() != null) {
                // 记录事务日志
                transactionLogService.saveLog(
                        result.getTransactionId(),
                        MqConstants.TOPIC_TRANSACTION,
                        event.getEventType(),
                        "TRADE",
                        messageBody
                );
                log.info("交易事务消息发送成功，订单号: {}, transactionId: {}",
                        event.getOrderId(), result.getTransactionId());
                return result.getTransactionId();
            }

            log.error("交易事务消息发送失败，订单号: {}", event.getOrderId());
            return null;
        } catch (Exception e) {
            log.error("发送交易事务消息异常，订单号: {}", event.getOrderId(), e);
            return null;
        }
    }
}