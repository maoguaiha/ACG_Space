-- ==============================================
-- ACG Space V1.1 数据库迁移脚本
-- 社区系统 + 用户主页 + 评论回复 + 用户发文
-- ==============================================

-- 1. 用户关注表
DROP TABLE IF EXISTS `biz_user_follow`;
CREATE TABLE `biz_user_follow` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '关注者ID',
  `follow_user_id` bigint(20) NOT NULL COMMENT '被关注者ID',
  `create_time` datetime DEFAULT NULL COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_follow` (`user_id`, `follow_user_id`),
  KEY `idx_follow_user_id` (`follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注表';

-- 2. 系统用户表扩展字段
ALTER TABLE `sys_user`
  ADD COLUMN `points` int(11) DEFAULT '0' COMMENT '总积分' AFTER `email`,
  ADD COLUMN `bio` varchar(500) DEFAULT NULL COMMENT '个人简介' AFTER `points`,
  ADD COLUMN `follower_count` int(11) DEFAULT '0' COMMENT '粉丝数' AFTER `bio`,
  ADD COLUMN `following_count` int(11) DEFAULT '0' COMMENT '关注数' AFTER `follower_count`;

-- 3. 文章评论表增加回复目标用户字段
ALTER TABLE `biz_comment`
  ADD COLUMN `reply_to_user_id` bigint(20) DEFAULT NULL COMMENT '回复目标用户ID' AFTER `parent_id`,
  ADD COLUMN `reply_to_nickname` varchar(30) DEFAULT NULL COMMENT '回复目标用户昵称' AFTER `reply_to_user_id`;

ALTER TABLE `biz_article_comment`
  ADD COLUMN `reply_to_user_id` bigint(20) DEFAULT NULL COMMENT '回复目标用户ID' AFTER `parent_id`,
  ADD COLUMN `reply_to_nickname` varchar(30) DEFAULT NULL COMMENT '回复目标用户昵称' AFTER `reply_to_user_id`;

-- 4. 文章表增加审核字段
ALTER TABLE `biz_article`
  ADD COLUMN `reject_reason` varchar(500) DEFAULT NULL COMMENT '驳回原因' AFTER `is_featured`;

-- 5. 番剧评论点赞点踩记录表
DROP TABLE IF EXISTS `biz_comment_reaction`;
CREATE TABLE `biz_comment_reaction` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `comment_id` bigint(20) NOT NULL COMMENT '评论ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `reaction_type` tinyint(4) NOT NULL COMMENT '反应类型 (1点赞 2点踩)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
  KEY `idx_comment_id` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='番剧评论点赞点踩记录表';

-- 6. 文章评论点赞点踩记录表
DROP TABLE IF EXISTS `biz_article_comment_reaction`;
CREATE TABLE `biz_article_comment_reaction` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `comment_id` bigint(20) NOT NULL COMMENT '评论ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `reaction_type` tinyint(4) NOT NULL COMMENT '反应类型 (1点赞 2点踩)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
  KEY `idx_comment_id` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章评论点赞点踩记录表';
