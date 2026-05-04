package com.ruoyi.project.mq.consumer;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.project.common.constant.MqConstants;
import com.ruoyi.project.domain.dto.CommentEventDTO;
import com.ruoyi.project.service.IBizUserPointsLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 评论积分事件消费者
 * 监听评论产生的消息，异步计算并派发积分
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
    topic = MqConstants.TOPIC_COMMENT_EVENT,
    consumerGroup = MqConstants.CONSUMER_GROUP_COMMENT
)
public class CommentPointsConsumer implements RocketMQListener<String> {

    private final IBizUserPointsLogService userPointsLogService;

    @Override
    public void onMessage(String message) {
        log.info("RocketMQ 接收到评论事件消息: {}", message);

        try {
            // 1. 反序列化消息体
            CommentEventDTO event = JSON.parseObject(message, CommentEventDTO.class);
            if (event.getUserId() == null || event.getCommentId() == null) {
                log.error("消息格式错误，缺少关键字段: {}", message);
                return;
            }

            // 2. 调用服务层执行积分发放逻辑 (内部已实现基于 DB 唯一索引的幂等性校验)
            userPointsLogService.addPointsForComment(event.getUserId(), event.getCommentId());

        } catch (Exception e) {
            log.error("消费评论事件消息失败, message: {}", message, e);
            // 抛出异常使得 RocketMQ 触发重试机制
            throw new RuntimeException("消费失败，触发重试", e);
        }
    }
}
