-- ==============================================
-- ACG Space 数据库迁移汇总脚本
-- 生成时间: 2026-05-06
-- 说明: 此文件汇总 `backend/sql/` 下的主要迁移脚本，按历史顺序排列。
-- 请在非生产环境先执行并校验，注意外键检查和幂等性。
-- ==============================================

-- TOC:
-- 1. v1.1_all_in_one.sql
-- 2. v1.2_user_vip_level_migration.sql
-- 3. schema.sql
-- 4. v2.0_digital_asset_migration.sql
-- 5. v2.1_synthesize_migration.sql
-- 6. 其他修复脚本（fix_*.sql, insert_test_user.sql, migrate_avatar.sql, modify_cover_url.sql, update_user_points.sql 等）

-- =========================
-- Begin: v1.1_all_in_one.sql
-- Source: backend/sql/v1.1_all_in_one.sql
-- =========================

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

-- =========================
-- End: v1.1_all_in_one.sql
-- =========================


-- =========================
-- Begin: v1.2_user_vip_level_migration.sql
-- Source: backend/sql/v1.2_user_vip_level_migration.sql
-- =========================

-- ==============================================
-- 用户VIP状态和等级迁移
-- ==============================================

-- 添加VIP相关字段到sys_user表
ALTER TABLE `sys_user`
  ADD COLUMN `vip_status` TINYINT NOT NULL DEFAULT 0 COMMENT 'VIP状态 (0=无VIP,1=VIP,2=SVIP)' AFTER `following_count`,
  ADD COLUMN `vip_expire_time` DATETIME DEFAULT NULL COMMENT 'VIP到期时间' AFTER `vip_status`,
  ADD COLUMN `user_level` INT NOT NULL DEFAULT 1 COMMENT '用户等级 (1-100)' AFTER `vip_expire_time`,
  ADD COLUMN `level_experience` INT NOT NULL DEFAULT 0 COMMENT '当前经验值' AFTER `user_level`;

-- 更新现有用户的默认等级（根据积分计算）
UPDATE `sys_user` SET `user_level` = GREATEST(1, LEAST(100, 1 + FLOOR(`points` / 500)));
DESCRIBE `sys_user`;

-- =========================
-- End: v1.2_user_vip_level_migration.sql
-- =========================


-- =========================
-- Begin: schema.sql
-- Source: backend/sql/schema.sql
-- =========================

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

-- (schema.sql continues...)

-- =========================
-- End: schema.sql (partial)
-- =========================


-- =========================
-- Begin: v2.0_digital_asset_migration.sql
-- Source: backend/sql/v2.0_digital_asset_migration.sql
-- =========================

-- ==============================================
-- ACG Space V2.0 数据库迁移脚本
-- 数字资产系统 & O2O核销系统
-- 执行时间: 2026-05-05
-- ==============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 物品/商品表 (biz_item)
-- ----------------------------
DROP TABLE IF EXISTS `biz_item`;
CREATE TABLE `biz_item` (
  `id` bigint(20) NOT NULL COMMENT '主键ID (Snowflake)',
  `item_key` varchar(100) NOT NULL COMMENT '物品唯一标识 (如 item_ssr_001)',
  `name` varchar(100) NOT NULL COMMENT '物品名称',
  `type` varchar(20) NOT NULL COMMENT '物品类型 (character/ weapon/skin/material)',
  `rarity` varchar(10) NOT NULL COMMENT '稀有度 (SSR/SR/R/N)',
  `image` varchar(500) DEFAULT NULL COMMENT '物品图片URL',
  `description` varchar(500) DEFAULT NULL COMMENT '物品描述',
  `total_stock` int(11) NOT NULL DEFAULT 0 COMMENT '总库存',
  `remaining_stock` int(11) NOT NULL DEFAULT 0 COMMENT '剩余库存',
  `price` int(11) NOT NULL DEFAULT 0 COMMENT '参考价格(积分)',
  `marketable` tinyint(1) DEFAULT 1 COMMENT '是否可上架市场 (0否 1是)',
  `synthesizable` tinyint(1) DEFAULT 0 COMMENT '是否可合成 (0否 1是)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志 (0存在 2删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_key` (`item_key`),
  KEY `idx_rarity` (`rarity`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品/商品表';

-- (v2.0 continued...)

-- =========================
-- End: v2.0_digital_asset_migration.sql (partial)
-- =========================


-- =========================
-- Begin: v2.1_synthesize_migration.sql
-- Source: backend/sql/v2.1_synthesize_migration.sql
-- =========================

-- ==============================================
-- ACG Space V2.0 合成系统数据库设计
-- 执行时间: 2026-05-05
-- ==============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 合成配方表 (biz_synthesize_recipe)
-- ----------------------------
DROP TABLE IF EXISTS `biz_synthesize_recipe`;
CREATE TABLE `biz_synthesize_recipe` (
  `id` bigint(20) NOT NULL COMMENT '主键ID (Snowflake)',
  `name` varchar(100) NOT NULL COMMENT '配方名称',
  `description` varchar(500) DEFAULT NULL COMMENT '配方描述',
  `result_item_id` bigint(20) NOT NULL COMMENT '产物物品ID',
  `result_quantity` int(11) NOT NULL DEFAULT 1 COMMENT '产物数量',
  `cost_type` varchar(20) NOT NULL DEFAULT 'materials' COMMENT '消耗类型 (materials=材料消耗 items=指定物品消耗)',
  `cost_items` text NOT NULL COMMENT '消耗材料配置 (JSON: [{"itemId":1,"count":3},{"itemId":2,"count":5}])',
  `cost_points` int(11) DEFAULT 0 COMMENT '额外消耗积分',
  `success_rate` int(11) NOT NULL DEFAULT 100 COMMENT '成功率 (%)',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 (0=禁用 1=启用)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志 (0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_result_item_id` (`result_item_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合成配方表';

-- (v2.1 continued...)

-- =========================
-- End: v2.1_synthesize_migration.sql
-- =========================


-- =========================
-- Other individual fix scripts
-- Source directory: backend/sql
-- Files: fix_all_remark_columns.sql, fix_all_remark_columns_safe.sql,
--        fix_biz_gacha_record_columns.sql, fix_item_name_columns.sql,
--        fix_remark_column.sql, fix_synthesize_remark.sql,
--        insert_test_user.sql, migrate_avatar.sql, modify_cover_url.sql,
--        add_article_reaction.sql, backfill_anime_status_once.sql, update_user_points.sql
-- These scripts are provided as discrete fixes;请根据需要单独审核并按顺序执行。

-- End of migration summary
