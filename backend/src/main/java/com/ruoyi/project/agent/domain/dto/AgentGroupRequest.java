package com.ruoyi.project.agent.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI 助手创建/重命名会话分组请求体（sortOrder 可选，新建时不传 = 默认 0）。
 */
@Data
public class AgentGroupRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分组名
     */
    @NotBlank(message = "分组名不能为空")
    @Size(max = 50, message = "分组名过长，请控制在 50 字以内")
    private String name;

    /**
     * 排序（越小越靠前）。可选，默认 0。
     */
    private Integer sortOrder;
}