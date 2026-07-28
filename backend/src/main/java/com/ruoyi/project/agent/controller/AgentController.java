package com.ruoyi.project.agent.controller;

import com.ruoyi.project.agent.domain.dto.AgentChatRequest;
import com.ruoyi.project.agent.domain.entity.AgentConversation;
import com.ruoyi.project.agent.service.IAgentService;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.exception.BizErrorCode;
import com.ruoyi.project.common.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 助手门面控制器（Java 守门面：鉴权 + 持久化 + SSE 代理）。
 * <p>
 * 所有端点均要求登录（不进 permitAll）；SSE 流式问答代理到 python-agent 内网服务。
 */
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
@Validated
public class AgentController {

    private final IAgentService agentService;

    /**
     * SSE 流式问答。需登录，限流 + 熔断在 Service 内手动防护。
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@Valid @RequestBody AgentChatRequest req) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new RuntimeException("未登录或登录已过期"));
            return emitter;
        }
        return agentService.chat(userId, req);
    }

    /**
     * 当前用户的会话列表（按更新时间倒序）。
     */
    @GetMapping("/conversations")
    public Result<List<AgentConversation>> listConversations() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.error(BizErrorCode.UNAUTHORIZED);
        }
        return Result.success(agentService.listConversations(userId));
    }

    /**
     * 新建一个空会话，返回会话ID（前端用于串联后续问答）。
     */
    @PostMapping("/conversations")
    public Result<String> createConversation() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.error(BizErrorCode.UNAUTHORIZED);
        }
        return Result.success(agentService.createConversation(userId));
    }

    /**
     * 删除会话（逻辑删除，同时级联逻辑删除其消息）。
     */
    @DeleteMapping("/conversations/{id}")
    public Result<Boolean> deleteConversation(@PathVariable String id) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.error(BizErrorCode.UNAUTHORIZED);
        }
        return Result.success(agentService.deleteConversation(userId, id));
    }
}
