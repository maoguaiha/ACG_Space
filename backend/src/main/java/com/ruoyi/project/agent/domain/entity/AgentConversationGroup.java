package com.ruoyi.project.agent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.project.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * AI 助手会话分组实体（用户自定义分组，对应 agent_conversation_group 表）。
 * <p>
 * 一个用户可建多个分组，每个分组可挂载多个会话；会话的 group_id 在 AgentConversation 中持有。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("agent_conversation_group")
public class AgentConversationGroup extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分组 ID（雪花算法）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属用户 ID
     */
    private Long userId;

    /**
     * 分组名（用户可自定义，如「番剧灵感」「学习资料」）
     */
    private String name;

    /**
     * 排序（越小越靠前，默认 0）
     */
    private Integer sortOrder;
}