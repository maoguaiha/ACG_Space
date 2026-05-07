-- ==============================================
-- ACG Space 数据库修复脚本
-- 修复表结构与实体类不匹配的问题
-- 执行时间: 2026-05-05
-- ==============================================

-- 1. 修复 biz_synthesize_recipe 表 - 添加 remark 列
ALTER TABLE biz_synthesize_recipe ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;

-- 2. 检查其他表是否也有 remark 列缺失问题
-- 如果有，添加类似上述的 ALTER TABLE 语句
