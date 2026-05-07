-- 修复所有表缺少 remark 列的问题
-- 使用存储过程安全添加列（如果不存在）

USE acg_space;

-- biz_synthesize_recipe
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'acg_space' AND TABLE_NAME = 'biz_synthesize_recipe' AND COLUMN_NAME = 'remark');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE biz_synthesize_recipe ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT ''备注'' AFTER update_time', 'SELECT ''Column remark already exists in biz_synthesize_recipe''');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- biz_synthesize_record
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'acg_space' AND TABLE_NAME = 'biz_synthesize_record' AND COLUMN_NAME = 'remark');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE biz_synthesize_record ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT ''备注'' AFTER update_time', 'SELECT ''Column remark already exists in biz_synthesize_record''');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- biz_item
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'acg_space' AND TABLE_NAME = 'biz_item' AND COLUMN_NAME = 'remark');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE biz_item ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT ''备注'' AFTER update_time', 'SELECT ''Column remark already exists in biz_item''');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- biz_gacha_record
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'acg_space' AND TABLE_NAME = 'biz_gacha_record' AND COLUMN_NAME = 'remark');
SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE biz_gacha_record ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT ''备注'' AFTER update_time', 'SELECT ''Column remark already exists in biz_gacha_record''');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- 连接到 MySQL 数据库
-- 使用命令：mysql -u root -proot

USE acg_space;

-- 为 biz_synthesize_recipe 表添加 remark 列
ALTER TABLE biz_synthesize_recipe ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;

-- 为其他表也添加 remark 列（如果缺少）
ALTER TABLE biz_synthesize_record ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;
ALTER TABLE biz_item ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;
ALTER TABLE biz_gacha_record ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;