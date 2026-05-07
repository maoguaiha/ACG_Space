package com.ruoyi.project.common.constant;

/**
 * 消息队列常量
 */
public class MqConstants {

    /**
     * 评论事件 Topic
     */
    public static final String TOPIC_COMMENT_EVENT = "TOPIC_COMMENT_EVENT";

    /**
     * 评论事件消费组
     */
    public static final String CONSUMER_GROUP_COMMENT = "cg_comment_points";

    /**
     * 交易事件 Topic
     */
    public static final String TOPIC_TRANSACTION = "TOPIC_TRANSACTION";

    /**
     * 交易事件消费组
     */
    public static final String CONSUMER_GROUP_TRANSACTION = "cg_transaction";

    /**
     * 交易事务TAG：资产转移
     */
    public static final String TAG_ASSET_TRANSFER = "asset_transfer";

    /**
     * 交易事务TAG：积分转移
     */
    public static final String TAG_POINTS_TRANSFER = "points_transfer";

}
