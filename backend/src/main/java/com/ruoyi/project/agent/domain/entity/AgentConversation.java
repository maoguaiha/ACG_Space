package com.ruoyi.project.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.project.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * AI 助手会话实体（Java 门面持久化，对应 agent_conversation 表）。
 * 仅保存会话元信息，向量/切片索引由 Python 侧持有，不落 MySQL。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("agent_conversation")
public class AgentConversation extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID（雪花算法）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 会话标题（取首条用户消息前 20 字）
     */
    private String title;

    /**
     * 是否置顶（0 否 / 1 是）。会话列表置顶会话优先返回。
     */
    private Integer pinned;

    /**
     * 所属分组 ID（NULL = 最近对话未分组）。
     */
    private Long groupId;
}
