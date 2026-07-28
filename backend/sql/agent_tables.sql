-- ============================================================
-- Agent 应用（用户端 AI 助手）会话 / 消息表 — Phase 4 门面持久化
-- 风格与项目现有表一致：id BIGINT 雪花主键、审计字段、del_flag 逻辑删除
-- 执行方式：
--   1) 全新库：可直接执行（CREATE TABLE IF NOT EXISTS 幂等）
--   2) 存量库：手动执行本文件即可补齐两张表
-- 向量/切片索引由 python-agent 内存持有，不落 MySQL。
-- ============================================================

CREATE TABLE IF NOT EXISTS `agent_conversation` (
  `id`         BIGINT       NOT NULL                COMMENT '会话ID(雪花算法)',
  `user_id`    BIGINT       NOT NULL                COMMENT '所属用户ID',
  `title`      VARCHAR(100) DEFAULT NULL            COMMENT '会话标题(取首条消息前20字)',
  `create_by`  VARCHAR(64)  DEFAULT NULL            COMMENT '创建者',
  `create_time` DATETIME     DEFAULT NULL           COMMENT '创建时间',
  `update_by`  VARCHAR(64)  DEFAULT NULL            COMMENT '更新者',
  `update_time` DATETIME     DEFAULT NULL           COMMENT '更新时间',
  `remark`     VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  `del_flag`   TINYINT(4)   DEFAULT 0              COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI助手会话表';

CREATE TABLE IF NOT EXISTS `agent_message` (
  `id`              BIGINT       NOT NULL            COMMENT '消息ID(雪花算法)',
  `conversation_id` BIGINT       NOT NULL            COMMENT '所属会话ID',
  `role`            VARCHAR(20)  NOT NULL            COMMENT '角色(user/assistant/system)',
  `content`         LONGTEXT     NOT NULL            COMMENT '消息内容',
  `tokens`          INT          DEFAULT NULL        COMMENT 'token数(可选, 便于用量统计)',
  `create_by`       VARCHAR(64)  DEFAULT NULL        COMMENT '创建者',
  `create_time`     DATETIME     DEFAULT NULL        COMMENT '创建时间',
  `update_by`       VARCHAR(64)  DEFAULT NULL        COMMENT '更新者',
  `update_time`     DATETIME     DEFAULT NULL        COMMENT '更新时间',
  `remark`          VARCHAR(500) DEFAULT NULL        COMMENT '备注',
  `del_flag`        TINYINT(4)   DEFAULT 0           COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_conv` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI助手消息表';
