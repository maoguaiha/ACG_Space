-- V1.3_article_content_longtext_migration.sql
-- 修改文章内容字段类型，支持更长的文章内容

ALTER TABLE `biz_article` MODIFY COLUMN `content` MEDIUMTEXT COMMENT '文章内容 (Markdown/富文本)';
