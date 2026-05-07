-- ==============================================
-- ACG Space 数据库修复脚本
-- 添加所有表缺失的 remark 列
-- 执行时间: 2026-05-05
-- ==============================================

USE acg_space;

-- 检查并添加 remark 列 (如果不存在)
-- biz_synthesize_recipe
ALTER TABLE biz_synthesize_recipe ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;

-- biz_synthesize_record
ALTER TABLE biz_synthesize_record ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;

-- biz_item (如果缺少)
ALTER TABLE biz_item ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;

-- biz_gacha_record (如果缺少)
ALTER TABLE biz_gacha_record ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;

-- biz_transaction (如果缺少)
ALTER TABLE biz_transaction ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;

-- biz_delivery_order (如果缺少)
ALTER TABLE biz_delivery_order ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;

-- biz_gacha_pool (如果缺少)
ALTER TABLE biz_gacha_pool ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;

-- biz_gacha_pool_item (如果缺少)
ALTER TABLE biz_gacha_pool_item ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;

-- biz_market_item (如果缺少)
ALTER TABLE biz_market_item ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;

-- biz_user_asset (如果缺少)
ALTER TABLE biz_user_asset ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;

-- biz_user_address (如果缺少)
ALTER TABLE biz_user_address ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;

-- 验证表结构
SHOW COLUMNS FROM biz_synthesize_recipe;
