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
}
