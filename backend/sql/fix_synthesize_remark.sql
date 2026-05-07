-- 修复合成配方表缺少 remark 列的问题
ALTER TABLE biz_synthesize_recipe ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER update_time;
