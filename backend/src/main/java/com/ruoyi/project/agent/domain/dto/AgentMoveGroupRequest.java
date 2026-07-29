package com.ruoyi.project.agent.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI 助手会话移动分组请求体（groupId 为 null 表示移回「最近对话」未分组）。
 */
@Data
public class AgentMoveGroupRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 目标分组 ID（NULL = 移回最近对话未分组）。
     * 用 String 接收：前端雪花 ID 超出 JS 安全整数范围，以字符串传递可避免精度丢失，
     * controller 再显式 Long.parseLong 还原。
     */
    private String groupId;
}