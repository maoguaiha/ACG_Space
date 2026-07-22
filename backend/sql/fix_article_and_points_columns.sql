-- =====================================================
-- Fix column mismatches: biz_article & biz_user_points_log
-- Date: 2026-07-10
-- Issue: Entity class field names don't match database column names.
--   1. biz_article: entity has summary, author_id, category, tags,
--      dislike_count, is_vip_only, is_featured, reject_reason
--      but DB table is missing all of them.
--   2. biz_user_points_log: entity has action_type, points_change,
--      biz_reference_id but DB table has type, points, source_id.
--   MyBatis-Plus generates SQL with entity column names -> "Unknown column" error.
-- =====================================================
SET NAMES utf8mb4;

-- ========== 1. biz_article: add missing columns ==========
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

-- ========== 2. biz_user_points_log: rename columns to match entity ==========
-- Entity expects: action_type, points_change, biz_reference_id
-- DB currently has: type, points, source_id

ALTER TABLE `biz_user_points_log`
  CHANGE COLUMN `type` `action_type` varchar(50) NOT NULL COMMENT '动作类型(如COMMENT,LOGIN,SHARE,REGISTRATION)',
  CHANGE COLUMN `points` `points_change` int(11) NOT NULL COMMENT '积分变动(正负值)',
  CHANGE COLUMN `source_id` `biz_reference_id` varchar(100) DEFAULT NULL COMMENT '业务关联ID(幂等键)';

-- Update unique key to use new column names
ALTER TABLE `biz_user_points_log` DROP INDEX `uk_user_type_source`;
ALTER TABLE `biz_user_points_log` ADD UNIQUE KEY `uk_user_action_ref` (`user_id`, `action_type`, `biz_reference_id`);
