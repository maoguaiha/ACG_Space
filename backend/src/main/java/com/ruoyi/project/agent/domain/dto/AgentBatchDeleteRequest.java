package com.ruoyi.project.agent.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * AI 助手批量删除会话请求体。
 */
@Data
public class AgentBatchDeleteRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 待删除的会话 ID 列表（前端为 string 防止精度丢失）。
     */
    @NotEmpty(message = "请选择至少一个会话")
    @Size(max = 100, message = "单次最多批量删除 100 个会话")
    private List<String> ids;
}