package com.ruoyi.project.agent.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI 助手对话请求体（前端 → Java 门面）。
 */
@Data
public class AgentChatRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID（首次对话可不传，由后端自动创建）
     */
    private String conversationId;

    /**
     * 用户本轮消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容过长，请控制在 2000 字以内")
    private String message;

    /**
     * 指定模型（可选，覆盖 python-agent 默认 LLM_CHAT_MODEL）。
     * 由前端「AI 设置」透传；为空时后端不传，python-agent 用默认值。
     */
    private String model;

    /**
     * 采样温度（可选，0~1）。为空时 python-agent 用默认值（0.3）。
     */
    private Double temperature;
}
