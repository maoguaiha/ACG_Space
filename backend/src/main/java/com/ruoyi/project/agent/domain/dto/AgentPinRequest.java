package com.ruoyi.project.agent.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI 助手会话置顶请求体。
 */
@Data
public class AgentPinRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * true = 置顶，false = 取消置顶
     */
    private Boolean pinned;
}