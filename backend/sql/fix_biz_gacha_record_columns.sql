-- ==============================================
-- ACG Space V2.0 修复脚本
-- 问题：biz_gacha_record 表缺少 create_by, update_by, remark 字段
-- 执行时间: 2026-05-05
-- ==============================================

SET NAMES utf8mb4;

-- 为 biz_gacha_record 表添加缺失字段
ALTER TABLE `biz_gacha_record`
ADD COLUMN `create_by` varchar(64) DEFAULT '' COMMENT '创建者' AFTER `del_flag`,
ADD COLUMN `update_by` varchar(64) DEFAULT '' COMMENT '更新者' AFTER `create_by`,
ADD COLUMN `remark` varchar(500) DEFAULT NULL COMMENT '备注' AFTER `update_by`;
