-- ==============================================
-- ACG Space 数据库迁移脚本 - 合并版
-- 包含所有历史迁移，确保幂等性
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
  ADD COLUMN `points` int(11) DEFAULT 0 COMMENT '总积分' AFTER `email`,
  ADD COLUMN `bio` varchar(500) DEFAULT NULL COMMENT '个人简介' AFTER `points`,
  ADD COLUMN `follower_count` int(11) DEFAULT 0 COMMENT '粉丝数' AFTER `bio`,
  ADD COLUMN `following_count` int(11) DEFAULT 0 COMMENT '关注数' AFTER `follower_count`;

-- 3. 文章评论回复字段
ALTER TABLE `biz_comment`
  ADD COLUMN `reply_to_user_id` bigint(20) DEFAULT NULL COMMENT '回复目标用户ID' AFTER `parent_id`,
  ADD COLUMN `reply_to_nickname` varchar(30) DEFAULT NULL COMMENT '回复目标用户昵称' AFTER `reply_to_user_id`,
  ADD COLUMN `dislikes` INT DEFAULT 0 COMMENT '点踩数' AFTER `likes`;

ALTER TABLE `biz_article_comment`
  ADD COLUMN `reply_to_user_id` bigint(20) DEFAULT NULL COMMENT '回复目标用户ID' AFTER `parent_id`,
  ADD COLUMN `reply_to_nickname` varchar(30) DEFAULT NULL COMMENT '回复目标用户昵称' AFTER `reply_to_user_id`,
  ADD COLUMN `dislikes` INT DEFAULT 0 COMMENT '点踩数' AFTER `likes`;

-- 4. 番剧评论点赞点踩记录表
DROP TABLE IF EXISTS `biz_comment_reaction`;
CREATE TABLE `biz_comment_reaction` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `comment_id` bigint(20) NOT NULL COMMENT '评论ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `reaction_type` tinyint(4) NOT NULL COMMENT '反应类型 (1点赞 2点踩)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
  KEY `idx_comment_id` (`comment_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='番剧评论点赞点踩记录表';

-- 5. 文章评论点赞点踩记录表
DROP TABLE IF EXISTS `biz_article_comment_reaction`;
CREATE TABLE `biz_article_comment_reaction` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `article_comment_id` bigint(20) NOT NULL COMMENT '文章评论ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `reaction_type` tinyint(4) NOT NULL COMMENT '反应类型 (1点赞 2点踩)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_comment_user` (`article_comment_id`, `user_id`),
  KEY `idx_article_comment_id` (`article_comment_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章评论点赞点踩记录表';

-- 6. 文章表字段扩展
ALTER TABLE `biz_article`
  ADD COLUMN `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (0=草稿,1=已发布,2=下架,3=待审核,4=驳回)' AFTER `del_flag`,
  ADD COLUMN `reject_reason` varchar(500) DEFAULT NULL COMMENT '驳回原因' AFTER `is_featured`,
  ADD COLUMN `dislike_count` int(11) DEFAULT 0 COMMENT '踩数' AFTER `like_count`;

ALTER TABLE `biz_article` MODIFY COLUMN `content` MEDIUMTEXT COMMENT '文章内容 (Markdown/富文本)';

-- 7. 私信表
DROP TABLE IF EXISTS `biz_message`;
CREATE TABLE `biz_message` (
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

-- 8. 番剧表类型字段
ALTER TABLE `biz_anime`
  ADD COLUMN `genre` varchar(255) DEFAULT NULL COMMENT '番剧类型（逗号分隔，如：热血,异世界,治愈）' AFTER `rating`,
  ADD KEY `idx_genre` (`genre`);

-- ==============================================
-- 迁移完成
-- ==============================================