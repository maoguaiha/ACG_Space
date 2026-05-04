-- v1.4_message_migration.sql
-- 私信功能数据库设计

-- 私信表
CREATE TABLE IF NOT EXISTS `biz_message` (
  `id` bigint(20) NOT NULL COMMENT '主键ID (Snowflake)',
  `from_user_id` bigint(20) NOT NULL COMMENT '发送者用户ID',
  `to_user_id` bigint(20) NOT NULL COMMENT '接收者用户ID',
  `content` varchar(1000) NOT NULL COMMENT '消息内容',
  `is_read` tinyint(1) DEFAULT 0 COMMENT '是否已读 (0-未读 1-已读)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_from_user` (`from_user_id`),
  KEY `idx_to_user` (`to_user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信表';

-- 消息会话视图（获取与某用户最新的消息和未读数）
-- 这个可以通过 SQL 查询实现，不需要创建视图
