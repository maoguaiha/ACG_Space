-- 修改封面图片字段类型以支持Base64编码
ALTER TABLE biz_article MODIFY COLUMN cover_url MEDIUMTEXT DEFAULT NULL COMMENT '封面图片链接';
