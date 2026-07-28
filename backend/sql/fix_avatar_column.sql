-- ============================================
-- 修复: avatar 列长度不足导致 Data truncation
-- 当前: varchar(500) → 改为 MEDIUMTEXT
-- 原因: base64 图片 URL / 长云存储链接超 500 字符
-- ============================================

ALTER TABLE sys_user
  MODIFY COLUMN `avatar` MEDIUMTEXT DEFAULT NULL COMMENT '头像';
