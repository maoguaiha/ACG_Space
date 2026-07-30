-- ============================================================
-- Agent 应用 V2.4 — 千问式侧边栏支撑（分组 + 置顶 + 批量管理）
--   - agent_conversation 加列: pinned (置顶), group_id (所属分组, NULL=未分组)
--   - 新表 agent_conversation_group (用户自定义会话分组)
--   - 复合索引 (user_id, pinned, update_time) 支撑置顶会话优先返回
--   - 索引 (user_id, group_id) 支撑按分组查询
-- 适用范围: 存量库升级（已存在 agent_conversation 表）。
--   MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS，故本脚本对老库只跑一次；
--   全新库请直接执行 agent_tables.sql（已包含本 V2.4 schema）。
-- ============================================================

SET NAMES utf8mb4;

-- 1) 给 agent_conversation 加列（先查再 ALTER，幂等）
SET @add_pinned = (
  SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
       WHERE table_schema = DATABASE()
         AND table_name = 'agent_conversation'
         AND column_name = 'pinned') = 0,
    'ALTER TABLE `agent_conversation` ADD COLUMN `pinned` TINYINT NOT NULL DEFAULT 0 COMMENT ''置顶(0否 1是)'' AFTER `title`',
    'SELECT 1'
  )
);
PREPARE s1 FROM @add_pinned; EXECUTE s1; DEALLOCATE PREPARE s1;

SET @add_group_id = (
  SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
       WHERE table_schema = DATABASE()
         AND table_name = 'agent_conversation'
         AND column_name = 'group_id') = 0,
    'ALTER TABLE `agent_conversation` ADD COLUMN `group_id` BIGINT DEFAULT NULL COMMENT ''所属分组ID(NULL=最近对话未分组)'' AFTER `pinned`',
    'SELECT 1'
  )
);
PREPARE s2 FROM @add_group_id; EXECUTE s2; DEALLOCATE PREPARE s2;

-- 2) 复合索引：按用户置顶优先 + 最近活跃排序
SET @add_idx_pin = (
  SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
       WHERE table_schema = DATABASE()
         AND table_name = 'agent_conversation'
         AND index_name = 'idx_user_pinned_update') = 0,
    'ALTER TABLE `agent_conversation` ADD INDEX `idx_user_pinned_update` (`user_id`, `pinned` DESC, `update_time` DESC)',
    'SELECT 1'
  )
);
PREPARE s3 FROM @add_idx_pin; EXECUTE s3; DEALLOCATE PREPARE s3;

-- 3) 按分组的查询索引
SET @add_idx_group = (
  SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
       WHERE table_schema = DATABASE()
         AND table_name = 'agent_conversation'
         AND index_name = 'idx_user_group') = 0,
    'ALTER TABLE `agent_conversation` ADD INDEX `idx_user_group` (`user_id`, `group_id`)',
    'SELECT 1'
  )
);
PREPARE s4 FROM @add_idx_group; EXECUTE s4; DEALLOCATE PREPARE s4;

-- 4) 新表：用户自定义会话分组
CREATE TABLE IF NOT EXISTS `agent_conversation_group` (
  `id`         BIGINT       NOT NULL                COMMENT '分组ID(雪花算法)',
  `user_id`    BIGINT       NOT NULL                COMMENT '所属用户ID',
  `name`       VARCHAR(50)  NOT NULL                COMMENT '分组名',
  `sort_order` INT          NOT NULL DEFAULT 0      COMMENT '排序(越小越靠前)',
  `create_by`  VARCHAR(64)  DEFAULT NULL            COMMENT '创建者',
  `create_time` DATETIME     DEFAULT NULL           COMMENT '创建时间',
  `update_by`  VARCHAR(64)  DEFAULT NULL            COMMENT '更新者',
  `update_time` DATETIME     DEFAULT NULL           COMMENT '更新时间',
  `remark`     VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  `del_flag`   TINYINT      DEFAULT 0               COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user_sort` (`user_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI助手会话分组表';

-- 5) 验证
SELECT '==== V2.4 upgrade done ====' AS info;
SELECT column_name, data_type, column_default
  FROM INFORMATION_SCHEMA.COLUMNS
 WHERE table_schema = DATABASE() AND table_name = 'agent_conversation' AND column_name IN ('pinned','group_id');
SELECT index_name, column_name, seq_in_index
  FROM INFORMATION_SCHEMA.STATISTICS
 WHERE table_schema = DATABASE() AND table_name = 'agent_conversation' AND index_name IN ('idx_user_pinned_update','idx_user_group')
 ORDER BY index_name, seq_in_index;
SELECT table_name FROM INFORMATION_SCHEMA.TABLES
 WHERE table_schema = DATABASE() AND table_name = 'agent_conversation_group';