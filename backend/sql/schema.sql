-- ----------------------------
-- 1. 番剧核心表
-- ----------------------------
DROP TABLE IF EXISTS `biz_anime`;
CREATE TABLE `biz_anime` (
  `id` bigint(20) NOT NULL COMMENT '主键ID (Snowflake)',
  `bgm_id` int(11) DEFAULT NULL COMMENT 'Bangumi 关联ID',
  `title` varchar(255) NOT NULL COMMENT '番剧名称',
  `title_original` varchar(255) DEFAULT NULL COMMENT '原版名称',
  `cover_url` varchar(500) DEFAULT NULL COMMENT '海报图片链接',
  `summary` text COMMENT '剧情简介',
  `total_episodes` int(11) DEFAULT '0' COMMENT '总集数',
  `publish_year` int(11) DEFAULT NULL COMMENT '开播年份',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态 (0连载中 1已完结 2未开播)',
  `rating` decimal(3,1) DEFAULT '0.0' COMMENT '综合评分',
  `featured` tinyint(4) DEFAULT '0' COMMENT '是否首页轮播推荐（0否 1是）',

  -- 若依框架标准通用字段
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bgm_id` (`bgm_id`),
  KEY `idx_publish_year` (`publish_year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='番剧信息表';

-- ----------------------------
-- 2. 评论互动表
-- ----------------------------
DROP TABLE IF EXISTS `biz_comment`;
CREATE TABLE `biz_comment` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `anime_id` bigint(20) NOT NULL COMMENT '所属番剧ID',
  `user_id` bigint(20) NOT NULL COMMENT '发布用户ID',
  `content` text NOT NULL COMMENT '评论内容',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父评论ID (回复树)',
  `likes` int(11) DEFAULT '0' COMMENT '点赞数',

  -- 若依通用字段
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_anime_id` (`anime_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='番剧评论互动表';

-- ----------------------------
-- 3. 积分流水表 (火箭MQ异步消费)
-- ----------------------------
DROP TABLE IF EXISTS `biz_user_points_log`;
CREATE TABLE `biz_user_points_log` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `action_type` varchar(50) NOT NULL COMMENT '动作类型 (如 COMMENT, LOGIN, SHARE)',
  `points_change` int(11) NOT NULL COMMENT '积分变动 (正负值)',
  `biz_reference_id` varchar(64) DEFAULT NULL COMMENT '业务关联ID (防止重复发放的幂等键)',

  -- 若依通用字段
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '变动说明',
  `del_flag` tinyint(4) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_ref` (`action_type`, `biz_reference_id`),
  KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分变动流水表';

-- ----------------------------
-- 4. 系统用户表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL COMMENT '用户ID',
  `username` varchar(30) NOT NULL COMMENT '用户账号',
  `nickname` varchar(30) NOT NULL COMMENT '用户昵称',
  `password` varchar(100) DEFAULT '' COMMENT '密码',
  `avatar` mediumtext DEFAULT NULL COMMENT '头像',
  `email` varchar(50) DEFAULT '' COMMENT '邮箱',

  -- 若依通用字段
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ----------------------------
-- 5. 番剧追番表
-- ----------------------------
DROP TABLE IF EXISTS `biz_anime_follow`;
CREATE TABLE `biz_anime_follow` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `anime_id` bigint(20) NOT NULL COMMENT '番剧ID',
  `create_time` datetime DEFAULT NULL COMMENT '追番时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_anime` (`user_id`, `anime_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='番剧追番记录表';

-- ----------------------------
-- 6. 博客文章表
-- ----------------------------
DROP TABLE IF EXISTS `biz_article`;
CREATE TABLE `biz_article` (
  `id` bigint(20) NOT NULL COMMENT '主键ID (Snowflake)',
  `title` varchar(255) NOT NULL COMMENT '文章标题',
  `summary` varchar(500) DEFAULT NULL COMMENT '文章摘要',
  `content` text COMMENT '文章内容 (Markdown/富文本)',
  `cover_url` mediumtext DEFAULT NULL COMMENT '封面图片链接',
  `author_id` bigint(20) NOT NULL COMMENT '作者用户ID',
  `category` varchar(50) DEFAULT NULL COMMENT '文章分类',
  `tags` varchar(255) DEFAULT NULL COMMENT '标签 (逗号分隔)',
  `view_count` int(11) DEFAULT '0' COMMENT '浏览量',
  `like_count` int(11) DEFAULT '0' COMMENT '点赞数',
  `comment_count` int(11) DEFAULT '0' COMMENT '评论数',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态 (0草稿 1发布 2下架)',
  `is_vip_only` tinyint(4) DEFAULT '0' COMMENT '是否VIP专享 (0否 1是)',
  `is_featured` tinyint(4) DEFAULT '0' COMMENT '是否推荐 (0否 1是)',

  -- 若依框架标准通用字段
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_author_id` (`author_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客文章表';

-- ----------------------------
-- 7. 文章评论表
-- ----------------------------
DROP TABLE IF EXISTS `biz_article_comment`;
CREATE TABLE `biz_article_comment` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `article_id` bigint(20) NOT NULL COMMENT '所属文章ID',
  `user_id` bigint(20) NOT NULL COMMENT '发布用户ID',
  `content` text NOT NULL COMMENT '评论内容',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父评论ID (回复树)',
  `likes` int(11) DEFAULT '0' COMMENT '点赞数',

  -- 若依通用字段
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章评论表';

-- ----------------------------
-- 8. RocketMQ事务日志回查表
-- ----------------------------
DROP TABLE IF EXISTS `biz_transaction_log`;
CREATE TABLE `biz_transaction_log` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `transaction_id` varchar(64) NOT NULL COMMENT 'RocketMQ事务消息ID',
  `topic` varchar(128) NOT NULL COMMENT '消息主题',
  `tag` varchar(128) DEFAULT NULL COMMENT '消息标签',
  `status` tinyint(4) NOT NULL COMMENT '事务状态 (0准备中 1提交 2回滚)',
  `business_type` varchar(50) DEFAULT NULL COMMENT '业务类型 (TRADE_BUY, TRADE_SELL等)',
  `business_data` text COMMENT '业务数据JSON',
  `check_count` int(11) DEFAULT '0' COMMENT '回查次数',
  `last_check_time` datetime DEFAULT NULL COMMENT '最后回查时间',
  `error_message` text COMMENT '错误信息',

  -- 若依通用字段
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transaction_id` (`transaction_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RocketMQ事务日志回查表';