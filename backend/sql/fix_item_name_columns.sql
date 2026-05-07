-- 修复用户资产表缺少的列
ALTER TABLE biz_user_asset
ADD COLUMN item_name VARCHAR(100) DEFAULT NULL COMMENT '物品名称' AFTER item_id;
ALTER TABLE biz_user_asset
ADD COLUMN item_image VARCHAR(500) DEFAULT NULL COMMENT '物品图片' AFTER item_name;
ALTER TABLE biz_user_asset
ADD COLUMN item_rarity VARCHAR(20) DEFAULT NULL COMMENT '物品稀有度' AFTER item_image;
ALTER TABLE biz_user_asset
ADD COLUMN item_type VARCHAR(50) DEFAULT NULL COMMENT '物品类型' AFTER item_rarity;

-- 修复交易表缺少的列
ALTER TABLE biz_transaction
ADD COLUMN item_name VARCHAR(100) DEFAULT NULL COMMENT '物品名称' AFTER item_id;

-- 修复配送订单表缺少的列
ALTER TABLE biz_delivery_order
ADD COLUMN item_name VARCHAR(100) DEFAULT NULL COMMENT '物品名称' AFTER item_id;

-- 修复市场物品表缺少的列
ALTER TABLE biz_market_item
ADD COLUMN item_name VARCHAR(100) DEFAULT NULL COMMENT '物品名称' AFTER item_id;





USE acg_space;

-- 检查 biz_user_asset 表结构
DESCRIBE biz_user_asset;

-- 如果没有 item_name 列，执行以下添加
ALTER TABLE biz_user_asset ADD COLUMN item_name VARCHAR(100) DEFAULT NULL COMMENT '物品名称' AFTER item_id;
ALTER TABLE biz_user_asset ADD COLUMN item_image VARCHAR(500) DEFAULT NULL COMMENT '物品图片' AFTER item_name;
ALTER TABLE biz_user_asset ADD COLUMN item_rarity VARCHAR(20) DEFAULT NULL COMMENT '物品稀有度' AFTER item_image;
ALTER TABLE biz_user_asset ADD COLUMN item_type VARCHAR(50) DEFAULT NULL COMMENT '物品类型' AFTER item_rarity;

-- 检查其他表
DESCRIBE biz_transaction;
DESCRIBE biz_delivery_order;
DESCRIBE biz_market_item;


-- 添加缺失的列（如果不存在）
ALTER TABLE biz_transaction ADD COLUMN item_name VARCHAR(100) DEFAULT NULL COMMENT '物品名称' AFTER item_id;
ALTER TABLE biz_delivery_order ADD COLUMN item_name VARCHAR(100) DEFAULT NULL COMMENT '物品名称' AFTER item_id;
ALTER TABLE biz_market_item ADD COLUMN item_name VARCHAR(100) DEFAULT NULL COMMENT '物品名称' AFTER item_id;
DESCRIBE biz_user_asset;

USE acg_space;

-- 添加缺失的审计字段到用户资产表
ALTER TABLE biz_user_asset ADD COLUMN create_by VARCHAR(64) DEFAULT NULL COMMENT '创建者' AFTER certified_time;
ALTER TABLE biz_user_asset ADD COLUMN update_by VARCHAR(64) DEFAULT NULL COMMENT '更新者' AFTER create_time;

-- 添加缺失的审计字段到合成配方表 (如果缺少)
ALTER TABLE biz_synthesize_recipe ADD COLUMN create_by VARCHAR(64) DEFAULT NULL COMMENT '创建者' AFTER status;
ALTER TABLE biz_synthesize_recipe ADD COLUMN update_by VARCHAR(64) DEFAULT NULL COMMENT '更新者' AFTER create_time;

-- 添加缺失的审计字段到合成记录表 (如果缺少)
ALTER TABLE biz_synthesize_record ADD COLUMN create_by VARCHAR(64) DEFAULT NULL COMMENT '创建者' AFTER status;
ALTER TABLE biz_synthesize_record ADD COLUMN update_by VARCHAR(64) DEFAULT NULL COMMENT '更新者' AFTER create_time;

-- 添加缺失的审计字段到物品表 (如果缺少)
ALTER TABLE biz_item ADD COLUMN create_by VARCHAR(64) DEFAULT NULL COMMENT '创建者' AFTER marketable;
ALTER TABLE biz_item ADD COLUMN update_by VARCHAR(64) DEFAULT NULL COMMENT '更新者' AFTER create_time;






-- 添加缺失的审计字段到合成记录表 (如果缺少)
ALTER TABLE biz_synthesize_record ADD COLUMN create_by VARCHAR(64) DEFAULT NULL COMMENT '创建者' AFTER status;
ALTER TABLE biz_synthesize_record ADD COLUMN update_by VARCHAR(64) DEFAULT NULL COMMENT '更新者' AFTER create_time;

-- 添加缺失的审计字段到物品表 (如果缺少)
ALTER TABLE biz_item ADD COLUMN create_by VARCHAR(64) DEFAULT NULL COMMENT '创建者' AFTER marketable;
ALTER TABLE biz_item ADD COLUMN update_by VARCHAR(64) DEFAULT NULL COMMENT '更新者' AFTER create_time;

ALTER TABLE biz_item ADD COLUMN update_by VARCHAR(64) DEFAULT NULL COMMENT '更新者' AFTER create_time;


USE acg_space;

ALTER TABLE biz_user_address ADD COLUMN create_by VARCHAR(64) DEFAULT NULL COMMENT '创建者' AFTER status;
ALTER TABLE biz_user_address ADD COLUMN update_by VARCHAR(64) DEFAULT NULL COMMENT '更新者' AFTER create_time;


ALTER TABLE biz_user_address ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_by;



USE acg_space;

-- 检查 sys_user 表结构
DESCRIBE sys_user;

-- 添加缺失的审计字段（如果不存在）
ALTER TABLE sys_user ADD COLUMN create_by VARCHAR(64) DEFAULT NULL COMMENT '创建者' AFTER login_date;
ALTER TABLE sys_user ADD COLUMN update_by VARCHAR(64) DEFAULT NULL COMMENT '更新者' AFTER create_time;
ALTER TABLE sys_user ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;