-- =====================================================
-- 修复 biz_comment 表缺失列
-- 问题: 实体 BizComment 有 anime_id/parent_id/reply_to_user_id
--       等字段，但老数据库表缺少这些列
-- 用法: 在 Railway MySQL 终端中执行
-- =====================================================

SET NAMES utf8mb4;

-- 使用存储过程安全添加列（不报错如果列已存在）
DROP PROCEDURE IF EXISTS SafeAddCommentColumns;
DELIMITER //
CREATE PROCEDURE SafeAddCommentColumns()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_comment' AND COLUMN_NAME = 'anime_id') THEN
        ALTER TABLE biz_comment ADD COLUMN `anime_id` bigint(20) DEFAULT NULL COMMENT '番剧ID' AFTER `id`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_comment' AND COLUMN_NAME = 'parent_id') THEN
        ALTER TABLE biz_comment ADD COLUMN `parent_id` bigint(20) DEFAULT '0' COMMENT '父评论ID' AFTER `content`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_comment' AND COLUMN_NAME = 'reply_to_user_id') THEN
        ALTER TABLE biz_comment ADD COLUMN `reply_to_user_id` bigint(20) DEFAULT NULL COMMENT '回复目标用户ID' AFTER `parent_id`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_comment' AND COLUMN_NAME = 'reply_to_nickname') THEN
        ALTER TABLE biz_comment ADD COLUMN `reply_to_nickname` varchar(100) DEFAULT NULL COMMENT '回复目标用户昵称' AFTER `reply_to_user_id`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_comment' AND COLUMN_NAME = 'likes') THEN
        ALTER TABLE biz_comment ADD COLUMN `likes` int(11) DEFAULT 0 COMMENT '点赞数' AFTER `reply_to_nickname`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_comment' AND COLUMN_NAME = 'dislikes') THEN
        ALTER TABLE biz_comment ADD COLUMN `dislikes` int(11) DEFAULT 0 COMMENT '点踩数' AFTER `likes`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_comment' AND COLUMN_NAME = 'create_by') THEN
        ALTER TABLE biz_comment ADD COLUMN `create_by` varchar(64) DEFAULT NULL COMMENT '创建者' AFTER `del_flag`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_comment' AND COLUMN_NAME = 'update_by') THEN
        ALTER TABLE biz_comment ADD COLUMN `update_by` varchar(64) DEFAULT NULL COMMENT '更新者' AFTER `create_by`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_comment' AND COLUMN_NAME = 'remark') THEN
        ALTER TABLE biz_comment ADD COLUMN `remark` varchar(500) DEFAULT NULL COMMENT '备注' AFTER `update_by`;
    END IF;
END //
DELIMITER ;

CALL SafeAddCommentColumns();
DROP PROCEDURE IF EXISTS SafeAddCommentColumns;

SELECT 'biz_comment 列修复完成' AS result;

-- 验证
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_comment'
ORDER BY ORDINAL_POSITION;
