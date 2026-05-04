-- 迁移脚本：将 sys_user.avatar 字段从 varchar(255) 改为 mediumtext
-- 用于存储 base64 编码的头像图片

USE acg_space;

ALTER TABLE sys_user 
MODIFY COLUMN avatar mediumtext DEFAULT NULL COMMENT '头像';

SELECT 'Avatar column migration completed successfully' as result;
