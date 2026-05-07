-- 修改 biz_item 表的 image 字段类型为 LONGTEXT 以支持 Base64 图片数据
ALTER TABLE `biz_item`
    MODIFY COLUMN `image` LONGTEXT DEFAULT NULL COMMENT '物品图片URL';

-- 同时修改其他可能存储长文本的字段
ALTER TABLE `biz_item`
    MODIFY COLUMN `description` LONGTEXT DEFAULT NULL COMMENT '物品描述';

-- 验证修改
SHOW COLUMNS FROM biz_item WHERE Field = 'image';