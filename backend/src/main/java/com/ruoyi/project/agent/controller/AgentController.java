package com.ruoyi.project.agent.controller;

import com.ruoyi.project.agent.domain.dto.AgentBatchDeleteRequest;
import com.ruoyi.project.agent.domain.dto.AgentChatRequest;
import com.ruoyi.project.agent.domain.dto.AgentGroupRequest;
import com.ruoyi.project.agent.domain.dto.AgentMoveGroupRequest;
import com.ruoyi.project.agent.domain.dto.AgentPinRequest;
import com.ruoyi.project.agent.domain.dto.AgentRenameRequest;
import com.ruoyi.project.agent.domain.entity.AgentConversation;
import com.ruoyi.project.agent.domain.entity.AgentConversationGroup;
import com.ruoyi.project.agent.domain.entity.AgentMessage;
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
import org.springframework.web.bind.annotation.PutMapping;
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
     * 查询指定会话的消息历史（按时间正序）。仅本人会话可查。
     */
    @GetMapping("/conversations/{id}/messages")
    public Result<List<AgentMessage>> listMessages(@PathVariable String id) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.error(BizErrorCode.UNAUTHORIZED);
        }
        return Result.success(agentService.listMessages(userId, id));
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

    /**
     * 重命名会话标题。
     */
    @PutMapping("/conversations/{id}")
    public Result<Boolean> renameConversation(@PathVariable String id,
                                              @Valid @RequestBody AgentRenameRequest req) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.error(BizErrorCode.UNAUTHORIZED);
        }
        return Result.success(agentService.renameConversation(userId, id, req.getTitle()));
    }

    /**
     * 清空当前用户的所有会话（级联删除消息）。
     */
    @DeleteMapping("/conversations")
    public Result<Boolean> clearConversations() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.error(BizErrorCode.UNAUTHORIZED);
        }
        return Result.success(agentService.clearConversations(userId));
    }

    // ====================== V2.4 千问式侧边栏 ======================

    /**
     * 置顶 / 取消置顶会话。仅本人可改。
     */
    @PutMapping("/conversations/{id}/pin")
    public Result<Boolean> pinConversation(@PathVariable String id,
                                           @Valid @RequestBody AgentPinRequest req) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.error(BizErrorCode.UNAUTHORIZED);
        }
        if (req.getPinned() == null) {
            return Result.error(BizErrorCode.BAD_REQUEST, "pinned 不能为空");
        }
        return Result.success(agentService.pinConversation(userId, id, req.getPinned()));
    }

    /**
     * 移动会话到指定分组（groupId 为 null 表示移回最近对话）。
     */
    @PutMapping("/conversations/{id}/group")
    public Result<Boolean> moveToGroup(@PathVariable String id,
                                       @Valid @RequestBody AgentMoveGroupRequest req) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.error(BizErrorCode.UNAUTHORIZED);
        }
        return Result.success(agentService.moveToGroup(userId, id, req.getGroupId()));
    }

    /**
     * 批量删除会话（仅本人会话生效）。
     */
    @DeleteMapping("/conversations/batch")
    public Result<Integer> batchDeleteConversations(@Valid @RequestBody AgentBatchDeleteRequest req) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.error(BizErrorCode.UNAUTHORIZED);
        }
        return Result.success(agentService.batchDeleteConversations(userId, req.getIds()));
    }

    /**
     * 当前用户的分组列表（按 sortOrder ASC）。
     */
    @GetMapping("/groups")
    public Result<List<AgentConversationGroup>> listGroups() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.error(BizErrorCode.UNAUTHORIZED);
        }
        return Result.success(agentService.listGroups(userId));
    }

    /**
     * 新建会话分组。
     */
    @PostMapping("/groups")
    public Result<String> createGroup(@Valid @RequestBody AgentGroupRequest req) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.error(BizErrorCode.UNAUTHORIZED);
        }
        return Result.success(agentService.createGroup(userId, req.getName(), req.getSortOrder()));
    }

    /**
     * 重命名分组。
     */
    @PutMapping("/groups/{id}")
    public Result<Boolean> renameGroup(@PathVariable String id,
                                       @Valid @RequestBody AgentGroupRequest req) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.error(BizErrorCode.UNAUTHORIZED);
        }
        return Result.success(agentService.renameGroup(userId, id, req.getName()));
    }

    /**
     * 删除分组（其下会话 group_id 自动归 NULL）。
     */
    @DeleteMapping("/groups/{id}")
    public Result<Boolean> deleteGroup(@PathVariable String id) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.error(BizErrorCode.UNAUTHORIZED);
        }
        return Result.success(agentService.deleteGroup(userId, id));
    }
}
