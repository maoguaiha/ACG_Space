package com.ruoyi.project.agent.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI 助手会话重命名请求体（前端 → Java 门面）。
 */
@Data
public class AgentRenameRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话新标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题过长，请控制在 50 字以内")
    private String title;
}
