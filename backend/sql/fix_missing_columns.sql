-- Fix missing columns for ACG_Space V2.1
SET NAMES utf8mb4;

ALTER TABLE `biz_gacha_pool`
  ADD COLUMN `rarity` varchar(20) DEFAULT NULL AFTER `banner`,
  ADD COLUMN `weight_config` text AFTER `status`,
  ADD COLUMN `create_by` varchar(64) DEFAULT NULL AFTER `weight_config`,
  ADD COLUMN `update_by` varchar(64) DEFAULT NULL AFTER `create_by`,
  ADD COLUMN `remark` varchar(500) DEFAULT NULL AFTER `update_by`;

ALTER TABLE `biz_anime` ADD COLUMN `create_by` varchar(64) DEFAULT NULL, ADD COLUMN `update_by` varchar(64) DEFAULT NULL, ADD COLUMN `remark` varchar(500) DEFAULT NULL;
ALTER TABLE `biz_article` ADD COLUMN `create_by` varchar(64) DEFAULT NULL, ADD COLUMN `update_by` varchar(64) DEFAULT NULL, ADD COLUMN `remark` varchar(500) DEFAULT NULL;
ALTER TABLE `biz_comment` ADD COLUMN `create_by` varchar(64) DEFAULT NULL, ADD COLUMN `update_by` varchar(64) DEFAULT NULL, ADD COLUMN `remark` varchar(500) DEFAULT NULL;
-- ========== biz_item entity-schema mismatch fix ==========
-- Issue: BizItem entity has item_key, total_stock, remaining_stock, price,
-- marketable, synthesizable fields but the DB table was missing them.
-- MyBatis-Plus generates INSERT with these columns → "Unknown column" error.
-- Note: MySQL 8.0 does not support "ADD COLUMN IF NOT EXISTS", so this
-- script should be run once on existing databases. For fresh installs,
-- ACG_Space_V2.1_Complete.sql already includes these columns.
ALTER TABLE `biz_item`
  ADD COLUMN `item_key` varchar(100) DEFAULT NULL COMMENT '物品唯一标识 (如 item_ssr_001)' AFTER `name`,
  ADD COLUMN `total_stock` int(11) DEFAULT 0 COMMENT '总库存' AFTER `description`,
  ADD COLUMN `remaining_stock` int(11) DEFAULT 0 COMMENT '剩余库存' AFTER `total_stock`,
  ADD COLUMN `price` int(11) DEFAULT 0 COMMENT '参考价格(积分)' AFTER `remaining_stock`,
  ADD COLUMN `marketable` tinyint(4) DEFAULT 1 COMMENT '是否可上架市场(0否1是)' AFTER `price`,
  ADD COLUMN `synthesizable` tinyint(4) DEFAULT 0 COMMENT '是否可合成(0否1是)' AFTER `marketable`,
  ADD COLUMN `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  ADD COLUMN `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  ADD COLUMN `remark` varchar(500) DEFAULT NULL COMMENT '备注';

-- Add index for item_key lookup
ALTER TABLE `biz_item` ADD INDEX `idx_item_key` (`item_key`);
ALTER TABLE `biz_gacha_pool_item` ADD COLUMN `create_by` varchar(64) DEFAULT NULL, ADD COLUMN `update_by` varchar(64) DEFAULT NULL, ADD COLUMN `remark` varchar(500) DEFAULT NULL;
ALTER TABLE `biz_gacha_record` ADD COLUMN `create_by` varchar(64) DEFAULT NULL, ADD COLUMN `update_by` varchar(64) DEFAULT NULL, ADD COLUMN `remark` varchar(500) DEFAULT NULL;
ALTER TABLE `biz_user_asset` ADD COLUMN `create_by` varchar(64) DEFAULT NULL, ADD COLUMN `update_by` varchar(64) DEFAULT NULL, ADD COLUMN `remark` varchar(500) DEFAULT NULL;
ALTER TABLE `biz_user_points_log` ADD COLUMN `create_by` varchar(64) DEFAULT NULL, ADD COLUMN `update_by` varchar(64) DEFAULT NULL, ADD COLUMN `remark` varchar(500) DEFAULT NULL;
ALTER TABLE `biz_market_item` ADD COLUMN `create_by` varchar(64) DEFAULT NULL, ADD COLUMN `update_by` varchar(64) DEFAULT NULL, ADD COLUMN `remark` varchar(500) DEFAULT NULL;

-- ========== biz_comment entity-schema mismatch fixes ==========
-- Issue: entity has animeId, likes, dislikes, replyToUserId, replyToNickname
-- but the DB table has target_type/target_id and like_count instead.
-- MyBatis-Plus generates WHERE anime_id=? which throws "Unknown column" SQL error.

ALTER TABLE `biz_comment`
  ADD COLUMN `anime_id` bigint(20) DEFAULT NULL COMMENT '番剧ID',
  ADD COLUMN `likes` int(11) DEFAULT 0 COMMENT '点赞数',
  ADD COLUMN `dislikes` int(11) DEFAULT 0 COMMENT '点踩数',
  ADD COLUMN `reply_to_user_id` bigint(20) DEFAULT NULL COMMENT '回复目标用户ID',
  ADD COLUMN `reply_to_nickname` varchar(100) DEFAULT NULL COMMENT '回复目标用户昵称';

-- Migrate existing data from old columns
UPDATE `biz_comment` SET `anime_id` = `target_id` WHERE `target_type` = 'anime' AND `anime_id` IS NULL;
UPDATE `biz_comment` SET `likes` = `like_count` WHERE `likes` = 0 AND `like_count` > 0;

-- ========== biz_message table missing ==========
-- Issue: register() calls sendRegistrationBonusMessage() which inserts into
-- biz_message, but the table doesn't exist in the database.
-- Fix: create the table for existing databases.
CREATE TABLE IF NOT EXISTS `biz_message` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `from_user_id` bigint(20) DEFAULT NULL COMMENT '发送者ID(0=系统)',
  `to_user_id` bigint(20) DEFAULT NULL COMMENT '接收者ID',
  `content` text COMMENT '消息内容',
  `is_read` tinyint(4) DEFAULT 0 COMMENT '是否已读(0否1是)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_to_user` (`to_user_id`),
  KEY `idx_from_user` (`from_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

-- ========== biz_anime_follow table missing ==========
-- Issue: getUserFollowList() queries biz_anime_follow, but the table doesn't
-- exist in the database. This causes "系统异常" when loading /follows page.
-- Fix: create the table for existing databases.
CREATE TABLE IF NOT EXISTS `biz_anime_follow` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `anime_id` bigint(20) NOT NULL COMMENT '番剧ID',
  `create_time` datetime DEFAULT NULL COMMENT '追番时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_anime` (`user_id`, `anime_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='番剧追番记录表';

-- ========== 11 missing entity tables (batch audit 2026-07-10) ==========
-- Issue: Entity classes with @TableName exist but their corresponding database
-- tables were never created. Each time a feature touches one of these entities,
-- it crashes with "Table 'acg_space.xxx' doesn't exist".
-- Fix: create all 11 missing tables at once.

-- 1. biz_comment_reaction
CREATE TABLE IF NOT EXISTS `biz_comment_reaction` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `comment_id` bigint(20) NOT NULL COMMENT '评论ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `reaction_type` int(11) DEFAULT NULL COMMENT '反应类型(1点赞 2点踩)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_comment_user` (`comment_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论反应表';

-- 2. biz_article_comment_reaction
CREATE TABLE IF NOT EXISTS `biz_article_comment_reaction` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `article_comment_id` bigint(20) NOT NULL COMMENT '文章评论ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `reaction_type` int(11) DEFAULT NULL COMMENT '反应类型(1点赞 2点踩)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_comment_user` (`article_comment_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章评论反应表';

-- 3. biz_article_reaction
CREATE TABLE IF NOT EXISTS `biz_article_reaction` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `article_id` bigint(20) NOT NULL COMMENT '文章ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `reaction_type` int(11) DEFAULT NULL COMMENT '反应类型(1点赞 2点踩)',
  `reason` varchar(500) DEFAULT NULL COMMENT '原因',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_article_user` (`article_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章反应表';

-- 4. biz_article_comment
CREATE TABLE IF NOT EXISTS `biz_article_comment` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `article_id` bigint(20) NOT NULL COMMENT '所属文章ID',
  `user_id` bigint(20) NOT NULL COMMENT '发布用户ID',
  `content` text COMMENT '评论内容',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父评论ID(0=顶级)',
  `reply_to_user_id` bigint(20) DEFAULT NULL COMMENT '回复目标用户ID',
  `reply_to_nickname` varchar(100) DEFAULT NULL COMMENT '回复目标用户昵称',
  `likes` int(11) DEFAULT 0 COMMENT '点赞数',
  `dislikes` int(11) DEFAULT 0 COMMENT '点踩数',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_article` (`article_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章评论表';

-- 5. biz_synthesize_record
CREATE TABLE IF NOT EXISTS `biz_synthesize_record` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `recipe_id` bigint(20) DEFAULT NULL COMMENT '配方ID',
  `recipe_name` varchar(100) DEFAULT NULL COMMENT '配方名称(冗余)',
  `result_item_id` bigint(20) DEFAULT NULL COMMENT '产物物品ID',
  `result_item_name` varchar(100) DEFAULT NULL COMMENT '产物名称(冗余)',
  `result_quantity` int(11) DEFAULT 1 COMMENT '产物数量',
  `cost_points` int(11) DEFAULT 0 COMMENT '消耗积分',
  `success` tinyint(1) DEFAULT 1 COMMENT '是否成功',
  `status` int(11) DEFAULT 2 COMMENT '状态(1=进行中 2=成功 3=失败)',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合成记录表';

-- 6. biz_synthesize_recipe
CREATE TABLE IF NOT EXISTS `biz_synthesize_recipe` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '配方名称',
  `description` text COMMENT '配方描述',
  `result_item_id` bigint(20) NOT NULL COMMENT '产物物品ID',
  `result_quantity` int(11) DEFAULT 1 COMMENT '产物数量',
  `cost_type` varchar(20) DEFAULT 'materials' COMMENT '消耗类型(materials=材料消耗 items=指定物品消耗)',
  `cost_items` text COMMENT '消耗材料配置(JSON)',
  `cost_points` int(11) DEFAULT 0 COMMENT '额外消耗积分',
  `success_rate` int(11) DEFAULT 100 COMMENT '成功率(%)',
  `status` int(11) DEFAULT 1 COMMENT '状态(0=禁用 1=启用)',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_result_item` (`result_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合成配方表';

-- 7. biz_delivery_order
CREATE TABLE IF NOT EXISTS `biz_delivery_order` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `order_id` varchar(64) NOT NULL COMMENT '订单号(DLV+时间戳+随机)',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `asset_id` bigint(20) DEFAULT NULL COMMENT '资产ID',
  `item_id` bigint(20) DEFAULT NULL COMMENT '物品ID',
  `item_name` varchar(100) DEFAULT NULL COMMENT '物品名称(冗余)',
  `item_image` varchar(500) DEFAULT NULL COMMENT '物品图片(冗余)',
  `item_rarity` varchar(10) DEFAULT NULL COMMENT '物品稀有度(冗余)',
  `receiver` varchar(50) DEFAULT NULL COMMENT '收货人姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(500) DEFAULT NULL COMMENT '详细地址',
  `express_company` varchar(100) DEFAULT NULL COMMENT '快递公司',
  `express_no` varchar(100) DEFAULT NULL COMMENT '快递单号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` int(11) DEFAULT 0 COMMENT '状态(0=待发货 1=已发货 2=已完成 3=已取消)',
  `ship_time` datetime DEFAULT NULL COMMENT '发货时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='O2O核销订单表';

-- 8. biz_transaction_log
CREATE TABLE IF NOT EXISTS `biz_transaction_log` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `transaction_id` varchar(100) DEFAULT NULL COMMENT 'RocketMQ事务消息ID',
  `topic` varchar(100) DEFAULT NULL COMMENT '消息主题',
  `tag` varchar(100) DEFAULT NULL COMMENT '消息标签',
  `status` int(11) DEFAULT 0 COMMENT '事务状态(0准备中 1提交 2回滚)',
  `business_type` varchar(50) DEFAULT NULL COMMENT '业务类型(TRADE_BUY,TRADE_SELL等)',
  `business_data` text COMMENT '业务数据JSON',
  `check_count` int(11) DEFAULT 0 COMMENT '回查次数',
  `last_check_time` datetime DEFAULT NULL COMMENT '最后回查时间',
  `error_message` text COMMENT '错误信息',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_transaction_id` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RocketMQ事务日志回查表';

-- 9. biz_user_address
CREATE TABLE IF NOT EXISTS `biz_user_address` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `receiver` varchar(50) DEFAULT NULL COMMENT '收货人姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `district` varchar(50) DEFAULT NULL COMMENT '区县',
  `detail_address` varchar(500) DEFAULT NULL COMMENT '详细地址',
  `postal_code` varchar(20) DEFAULT NULL COMMENT '邮政编码',
  `is_default` tinyint(4) DEFAULT 0 COMMENT '是否默认地址(0否 1是)',
  `status` int(11) DEFAULT 1 COMMENT '状态(1=正常 0=禁用)',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址表';

-- 10. biz_user_follow
CREATE TABLE IF NOT EXISTS `biz_user_follow` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '关注者ID',
  `follow_user_id` bigint(20) NOT NULL COMMENT '被关注者ID',
  `create_time` datetime DEFAULT NULL COMMENT '关注时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_follow` (`user_id`, `follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系表';

-- 11. biz_transaction
CREATE TABLE IF NOT EXISTS `biz_transaction` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `order_id` varchar(64) NOT NULL COMMENT '订单号(TXN+时间戳+随机)',
  `buyer_id` bigint(20) NOT NULL COMMENT '买家用户ID',
  `seller_id` bigint(20) NOT NULL COMMENT '卖家用户ID',
  `asset_id` bigint(20) DEFAULT NULL COMMENT '资产ID(用户资产表)',
  `item_id` bigint(20) DEFAULT NULL COMMENT '物品ID',
  `item_name` varchar(100) DEFAULT NULL COMMENT '物品名称(冗余)',
  `item_image` varchar(500) DEFAULT NULL COMMENT '物品图片(冗余)',
  `item_rarity` varchar(10) DEFAULT NULL COMMENT '物品稀有度(冗余)',
  `amount` int(11) DEFAULT 0 COMMENT '交易金额(积分)',
  `fee` int(11) DEFAULT 0 COMMENT '手续费(积分,1%)',
  `seller_amount` int(11) DEFAULT 0 COMMENT '卖家实得(积分)',
  `status` int(11) DEFAULT 0 COMMENT '状态(0=处理中 1=成功 2=失败 3=回查中)',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `rocketmq_tx_id` varchar(100) DEFAULT NULL COMMENT 'RocketMQ事务ID',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_buyer` (`buyer_id`),
  KEY `idx_seller` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单表';

-- ========== biz_article entity-schema mismatch fix (2026-07-10) ==========
-- Issue: BizArticle entity has summary, author_id, category, tags,
-- dislike_count, is_vip_only, is_featured, reject_reason fields,
-- but the DB table was missing all of them.
-- MyBatis-Plus generates SELECT with these columns -> "Unknown column" error.
-- Also: entity uses author_id but table had user_id. Added author_id
-- and migrated data from user_id.

ALTER TABLE `biz_article`
  ADD COLUMN `summary` varchar(500) DEFAULT NULL COMMENT '文章摘要' AFTER `title`,
  ADD COLUMN `author_id` bigint(20) DEFAULT NULL COMMENT '作者用户ID' AFTER `cover_url`,
  ADD COLUMN `category` varchar(50) DEFAULT NULL COMMENT '文章分类' AFTER `author_id`,
  ADD COLUMN `tags` varchar(500) DEFAULT NULL COMMENT '标签(逗号分隔)' AFTER `category`,
  ADD COLUMN `dislike_count` int(11) DEFAULT 0 COMMENT '点踩数' AFTER `like_count`,
  ADD COLUMN `is_vip_only` tinyint(4) DEFAULT 0 COMMENT '是否VIP专享(0否1是)' AFTER `status`,
  ADD COLUMN `is_featured` tinyint(4) DEFAULT 0 COMMENT '是否推荐(0否1是)' AFTER `is_vip_only`,
  ADD COLUMN `reject_reason` varchar(500) DEFAULT NULL COMMENT '驳回原因' AFTER `is_featured`;

-- Migrate data from user_id to author_id (entity uses author_id, not user_id)
UPDATE `biz_article` SET `author_id` = `user_id` WHERE `author_id` IS NULL;

-- Add index for author_id lookups
ALTER TABLE `biz_article` ADD INDEX `idx_author_id` (`author_id`);

-- ========== biz_user_points_log column name mismatch fix (2026-07-10) ==========
-- Issue: Entity has actionType/pointsChange/bizReferenceId which MyBatis-Plus
-- maps to action_type/points_change/biz_reference_id, but the DB table had
-- type/points/source_id. Every INSERT and WHERE clause failed with
-- "Unknown column 'action_type'" error.
-- Fix: rename columns to match entity expectations.

ALTER TABLE `biz_user_points_log`
  CHANGE COLUMN `type` `action_type` varchar(50) NOT NULL COMMENT '动作类型(如COMMENT,LOGIN,SHARE,REGISTRATION)',
  CHANGE COLUMN `points` `points_change` int(11) NOT NULL COMMENT '积分变动(正负值)',
  CHANGE COLUMN `source_id` `biz_reference_id` varchar(100) DEFAULT NULL COMMENT '业务关联ID(幂等键)';

-- Update unique key to use new column names
ALTER TABLE `biz_user_points_log` DROP INDEX `uk_user_type_source`;
ALTER TABLE `biz_user_points_log` ADD UNIQUE KEY `uk_user_action_ref` (`user_id`, `action_type`, `biz_reference_id`);

-- ========== update_time column missing on multiple tables (2026-07-10) ==========
-- Issue: The ALTER TABLE statements above added create_by/update_by/remark to
-- many tables but forgot update_time. Entity classes extend BaseEntity which
-- has updateTime. When setUpdateTime() is called, MyBatis-Plus generates
-- INSERT/UPDATE with update_time column -> "Unknown column 'update_time'" error.
-- Fix: add update_time to all affected tables.
-- Note: MySQL doesn't support ADD COLUMN IF NOT EXISTS. If a table already has
-- the column, that statement will error - just skip it and continue.

ALTER TABLE `biz_user_points_log` ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';
ALTER TABLE `biz_gacha_pool` ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';
ALTER TABLE `biz_anime` ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';
ALTER TABLE `biz_article` ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';
ALTER TABLE `biz_comment` ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';
ALTER TABLE `biz_item` ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';
ALTER TABLE `biz_gacha_pool_item` ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';
ALTER TABLE `biz_gacha_record` ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';
ALTER TABLE `biz_user_asset` ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';
ALTER TABLE `biz_market_item` ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';
