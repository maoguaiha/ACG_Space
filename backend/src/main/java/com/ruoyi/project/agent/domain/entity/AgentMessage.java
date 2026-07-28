package com.ruoyi.project.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.project.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * AI 助手消息实体（Java 门面持久化，对应 agent_message 表）。
 * role 取值：user / assistant / system。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("agent_message")
public class AgentMessage extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID（雪花算法）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属会话ID
     */
    private Long conversationId;

    /**
     * 角色：user / assistant / system
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * token 数（可选，便于后续用量统计）
     */
    private Integer tokens;
}
