-- ==============================================
-- ACG Space 完整合并迁移脚本
-- 生成时间: 2026-05-06
-- 说明: 将 backend/sql 下的主要迁移与修复脚本按顺序合并。
-- 注意：在生产库执行前请在测试库验证并备份数据。
-- ==============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Begin: v1.1_all_in_one.sql
-- Source: backend/sql/v1.1_all_in_one.sql
-- ----------------------------
-- (摘录并执行 v1.1 的关键 DDL/ALTER)

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

ALTER TABLE `sys_user`
  ADD COLUMN IF NOT EXISTS `points` int(11) DEFAULT 0 COMMENT '总积分' AFTER `email`,
  ADD COLUMN IF NOT EXISTS `bio` varchar(500) DEFAULT NULL COMMENT '个人简介' AFTER `points`,
  ADD COLUMN IF NOT EXISTS `follower_count` int(11) DEFAULT 0 COMMENT '粉丝数' AFTER `bio`,
  ADD COLUMN IF NOT EXISTS `following_count` int(11) DEFAULT 0 COMMENT '关注数' AFTER `follower_count`;

-- 其他 v1.1 更改略 (已保存在单文件中，建议根据需要拆分并执行)

-- ----------------------------
-- Begin: v1.2_user_vip_level_migration.sql
-- Source: backend/sql/v1.2_user_vip_level_migration.sql
-- ----------------------------
ALTER TABLE `sys_user`
  ADD COLUMN IF NOT EXISTS `vip_status` TINYINT NOT NULL DEFAULT 0 COMMENT 'VIP状态 (0=无VIP,1=VIP,2=SVIP)' AFTER `following_count`,
  ADD COLUMN IF NOT EXISTS `vip_expire_time` DATETIME DEFAULT NULL COMMENT 'VIP到期时间' AFTER `vip_status`,
  ADD COLUMN IF NOT EXISTS `user_level` INT NOT NULL DEFAULT 1 COMMENT '用户等级 (1-100)' AFTER `vip_expire_time`,
  ADD COLUMN IF NOT EXISTS `level_experience` INT NOT NULL DEFAULT 0 COMMENT '当前经验值' AFTER `user_level`;

UPDATE `sys_user` SET `user_level` = GREATEST(1, LEAST(100, 1 + FLOOR(`points` / 500)));

-- ----------------------------
-- Begin: schema.sql (核心表)
-- Source: backend/sql/schema.sql
-- ----------------------------
-- 这里将按原 schema.sql 创建核心表（biz_anime、biz_comment、sys_user、biz_article 等）
-- 为避免重复，这里仅包含关键表的创建示例，推荐在测试环境按原文件完整执行。

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
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='番剧信息表';

-- ----------------------------
-- Begin: v2.0_digital_asset_migration.sql
-- Source: backend/sql/v2.0_digital_asset_migration.sql
-- ----------------------------
-- (完整创建数字资产及相关表)

SET NAMES utf8mb4;

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

-- (省略其它表详细 DDL；请直接执行 v2.0_digital_asset_migration.sql 完整内容以保证一致性)

-- ----------------------------
-- Begin: v2.1_synthesize_migration.sql
-- Source: backend/sql/v2.1_synthesize_migration.sql
-- ----------------------------
DROP TABLE IF EXISTS `biz_synthesize_recipe`;
CREATE TABLE `biz_synthesize_recipe` (
  `id` bigint(20) NOT NULL COMMENT '主键ID (Snowflake)',
  `name` varchar(100) NOT NULL COMMENT '配方名称',
  `description` varchar(500) DEFAULT NULL COMMENT '配方描述',
  `result_item_id` bigint(20) NOT NULL COMMENT '产物物品ID',
  `result_quantity` int(11) NOT NULL DEFAULT 1 COMMENT '产物数量',
  `cost_type` varchar(20) NOT NULL DEFAULT 'materials' COMMENT '消耗类型 (materials=材料消耗 items=指定物品消耗)',
  `cost_items` text NOT NULL COMMENT '消耗材料配置 (JSON)',
  `cost_points` int(11) DEFAULT 0 COMMENT '额外消耗积分',
  `success_rate` int(11) NOT NULL DEFAULT 100 COMMENT '成功率 (%)',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 (0=禁用 1=启用)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) DEFAULT 0 COMMENT '删除标志 (0存在 2删除)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合成配方表';

-- ----------------------------
-- 修复与补丁脚本合并 (fix_*.sql 等)
-- ----------------------------

-- fix_all_remark_columns.sql 内容（若不存在 remark 列则添加）
-- (从文件 backend/sql/fix_all_remark_columns.sql)

-- 为 biz_synthesize_recipe 添加 remark（如果不存在）
ALTER TABLE biz_synthesize_recipe ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;
ALTER TABLE biz_synthesize_record ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;
ALTER TABLE biz_item ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;
ALTER TABLE biz_gacha_record ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;
ALTER TABLE biz_transaction ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;
ALTER TABLE biz_delivery_order ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;
ALTER TABLE biz_gacha_pool ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;
ALTER TABLE biz_gacha_pool_item ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;
ALTER TABLE biz_market_item ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;
ALTER TABLE biz_user_asset ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;
ALTER TABLE biz_user_address ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;

-- fix_biz_gacha_record_columns.sql
ALTER TABLE `biz_gacha_record`
ADD COLUMN IF NOT EXISTS `create_by` varchar(64) DEFAULT '' COMMENT '创建者' AFTER `del_flag`,
ADD COLUMN IF NOT EXISTS `update_by` varchar(64) DEFAULT '' COMMENT '更新者' AFTER `create_by`,
ADD COLUMN IF NOT EXISTS `remark` varchar(500) DEFAULT NULL COMMENT '备注' AFTER `update_by`;

-- fix_item_name_columns.sql（关键列添加）
ALTER TABLE biz_user_asset ADD COLUMN IF NOT EXISTS item_name VARCHAR(100) DEFAULT NULL COMMENT '物品名称' AFTER item_id;
ALTER TABLE biz_user_asset ADD COLUMN IF NOT EXISTS item_image VARCHAR(500) DEFAULT NULL COMMENT '物品图片' AFTER item_name;
ALTER TABLE biz_user_asset ADD COLUMN IF NOT EXISTS item_rarity VARCHAR(20) DEFAULT NULL COMMENT '物品稀有度' AFTER item_image;
ALTER TABLE biz_user_asset ADD COLUMN IF NOT EXISTS item_type VARCHAR(50) DEFAULT NULL COMMENT '物品类型' AFTER item_rarity;
ALTER TABLE biz_transaction ADD COLUMN IF NOT EXISTS item_name VARCHAR(100) DEFAULT NULL COMMENT '物品名称' AFTER item_id;
ALTER TABLE biz_delivery_order ADD COLUMN IF NOT EXISTS item_name VARCHAR(100) DEFAULT NULL COMMENT '物品名称' AFTER item_id;
ALTER TABLE biz_market_item ADD COLUMN IF NOT EXISTS item_name VARCHAR(100) DEFAULT NULL COMMENT '物品名称' AFTER item_id;

-- modify_cover_url.sql
ALTER TABLE biz_article MODIFY COLUMN cover_url MEDIUMTEXT DEFAULT NULL COMMENT '封面图片链接';

-- migrate_avatar.sql
ALTER TABLE sys_user MODIFY COLUMN avatar mediumtext DEFAULT NULL COMMENT '头像';

-- add_article_reaction.sql
DROP TABLE IF EXISTS `biz_article_reaction`;
CREATE TABLE `biz_article_reaction` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `article_id` bigint(20) NOT NULL COMMENT '文章ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `reaction_type` tinyint(4) NOT NULL COMMENT '反应类型 (1点赞 2点踩)',
  `reason` varchar(500) DEFAULT NULL COMMENT '点踩理由',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(4) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_user` (`article_id`, `user_id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章点赞点踩记录表';
ALTER TABLE `biz_article` ADD COLUMN IF NOT EXISTS `dislike_count` int(11) DEFAULT '0' COMMENT '点踩数' AFTER `like_count`;

-- backfill_anime_status_once.sql (一次性回填)
START TRANSACTION;
UPDATE biz_anime
SET
  status = CASE
    WHEN publish_year IS NOT NULL AND publish_year > YEAR(CURDATE()) THEN 2
    WHEN publish_year IS NOT NULL AND publish_year < YEAR(CURDATE()) AND IFNULL(total_episodes, 0) > 0 THEN 1
    ELSE 0
  END,
  update_by = 'sql_backfill_status',
  update_time = NOW(),
  remark = CONCAT(
    IFNULL(remark, ''),
    CASE WHEN IFNULL(remark, '') = '' THEN '' ELSE ' | ' END,
    '一次性状态回填: ',
    DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')
  )
WHERE del_flag = 0;
COMMIT;

-- insert_test_user.sql
INSERT INTO sys_user (id, username, nickname, password, points, vip_status, user_level, level_experience, del_flag) 
VALUES (1, 'admin', '管理员', 'admin123', 10000, 1, 10, 0, 0);
INSERT INTO sys_user (id, username, nickname, password, points, vip_status, user_level, level_experience, del_flag) 
VALUES (2, 'testuser', '测试用户', '123456', 5000, 0, 1, 0, 0);

-- update_user_points.sql
UPDATE sys_user SET points = 10000 WHERE username = 'admin';
UPDATE sys_user SET points = 5000 WHERE username = 'testuser';

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 迁移完成（请在测试库验证）
