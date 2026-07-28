package com.ruoyi.project.agent.service;

import com.ruoyi.project.agent.domain.dto.AgentChatRequest;
import com.ruoyi.project.agent.domain.entity.AgentConversation;
import com.ruoyi.project.agent.domain.entity.AgentMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 助手门面服务接口。
 */
public interface IAgentService {

    /**
     * SSE 流式问答：鉴权后的业务编排（限流/熔断 → 会话解析 → 持久化 → 代理 python-agent）。
     *
     * @param userId 当前登录用户ID
     * @param req    对话请求
     * @return SseEmitter 流式响应
     */
    SseEmitter chat(Long userId, AgentChatRequest req);

    /**
     * 当前用户的会话列表。
     */
    List<AgentConversation> listConversations(Long userId);

    /**
     * 新建会话，返回会话ID（字符串，避免前端精度丢失）。
     */
    String createConversation(Long userId);

    /**
     * 删除会话（逻辑删除 + 级联消息逻辑删除），仅本人可删。
     */
    boolean deleteConversation(Long userId, String id);

    /**
     * 重命名会话标题，仅本人可改。
     */
    boolean renameConversation(Long userId, String id, String title);

    /**
     * 清空当前用户的所有会话（级联删除其消息）。
     */
    boolean clearConversations(Long userId);

    /**
     * 查询指定会话的消息历史（按时间正序）。仅本人会话可查；越权或会话不存在返回空列表。
     *
     * @param userId        当前登录用户ID
     * @param conversationId 会话ID
     * @return 该会话的全部消息（按创建时间正序）
     */
    List<AgentMessage> listMessages(Long userId, String conversationId);
}
