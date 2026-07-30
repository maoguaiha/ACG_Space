package com.ruoyi.project.agent.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.project.agent.domain.dto.AgentChatRequest;
import com.ruoyi.project.agent.domain.entity.AgentConversation;
import com.ruoyi.project.agent.domain.entity.AgentConversationGroup;
import com.ruoyi.project.agent.domain.entity.AgentMessage;
import com.ruoyi.project.agent.mapper.AgentConversationGroupMapper;
import com.ruoyi.project.agent.mapper.AgentConversationMapper;
import com.ruoyi.project.agent.mapper.AgentMessageMapper;
import com.ruoyi.project.agent.service.IAgentService;
import com.ruoyi.project.common.exception.BizErrorCode;
import com.ruoyi.project.domain.entity.BaseEntity;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * AI 助手门面服务实现。
 * <p>
 * 流程：限流/熔断防护 → 解析或创建会话 → 落库用户消息 → 载入历史 → WebClient(SSE) 代理 python-agent
 * → 逐帧转发 token → 结束落库助手消息。
 * <p>
 * 说明：本项目 @RateLimiterAndCircuitBreaker 切面在限流命中时返回 Result，与 SseEmitter 返回类型不兼容，
 * 故此处直接通过注入的 Resilience4j Registry 手动应用限流 + 熔断，确保 SSE 流类型一致且熔断真正保护 python-agent。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements IAgentService {

    private static final long SSE_TIMEOUT_MS = Duration.ofMinutes(8).toMillis();
    private static final int HISTORY_LIMIT = 20;
    private static final String MSG_RATE_LIMITED = "请求过于频繁，请稍后再试";
    private static final String MSG_UNAVAILABLE = "AI 服务暂不可用，请稍后重试";
    private static final String MSG_TIMEOUT = "AI 响应超时，请稍后重试";

    private final AgentConversationMapper conversationMapper;
    private final AgentConversationGroupMapper groupMapper;
    private final AgentMessageMapper messageMapper;
    private final WebClient agentWebClient;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public SseEmitter chat(Long userId, AgentChatRequest req) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        // 1) 限流（agentStream 实例）
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("agentStream");
        if (!rateLimiter.acquirePermission()) {
            sendJson(emitter, Map.of("type", "error", "content", MSG_RATE_LIMITED));
            emitter.complete();
            return emitter;
        }

        // 2) 熔断（agentProxy 实例）：OPEN 时快速失败
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("agentProxy");
        if (!cb.tryAcquirePermission()) {
            sendJson(emitter, Map.of("type", "error", "content", MSG_UNAVAILABLE));
            emitter.complete();
            return emitter;
        }

        // 3) 解析/创建会话 + 落库用户消息 + 载入历史
        String conversationId = resolveConversation(userId, req.getConversationId(), req.getMessage());
        saveUserMessage(conversationId, userId, req.getMessage());
        List<Map<String, String>> history = loadHistory(Long.parseLong(conversationId), HISTORY_LIMIT);

        // 4) 组装转发体（与 python-agent /chat 契约一致）
        Map<String, Object> body = new LinkedHashMap<>(4);
        body.put("user_id", String.valueOf(userId));
        body.put("conversation_id", conversationId);
        body.put("message", req.getMessage());
        body.put("history", history);
        // 「AI 设置」透传：模型 / 温度（为空则不传，python-agent 用默认值）
        if (req.getModel() != null && !req.getModel().isBlank()) {
            body.put("model", req.getModel());
        }
        if (req.getTemperature() != null) {
            body.put("temperature", req.getTemperature());
        }
        // 附件（V1 仅文本）：内联文件名 + 内容，透传给 python-agent 注入上下文
        if (req.getAttachment() != null && req.getAttachment().getContent() != null
                && !req.getAttachment().getContent().isBlank()) {
            AgentChatRequest.AgentAttachment att = req.getAttachment();
            body.put("attachment", Map.of(
                    "filename", att.getFilename() == null ? "attachment" : att.getFilename(),
                    "content", att.getContent()
            ));
        }

        Flux<ServerSentEvent<String>> upstream = agentWebClient.post()
                .uri("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {
                })
                .timeout(Duration.ofMinutes(8));

        final StringBuilder assistantBuf = new StringBuilder();
        final boolean[] done = {false};
        final boolean[] errored = {false};
        final Disposable[] holder = {null};

        Disposable disposable = upstream.subscribe(
                sse -> {
                    String json = sse.data();
                    if (json == null || json.isBlank()) {
                        return;
                    }
                    try {
                        JSONObject obj = JSON.parseObject(json);
                        String type = obj.getString("type");
                        if ("token".equals(type)) {
                            String content = obj.getString("content");
                            if (content != null) {
                                assistantBuf.append(content);
                            }
                            // 原样转发 token 帧，前端解析 data:{json} 取 content
                            if (!sendJson(emitter, json)) {
                                if (holder[0] != null) {
                                    holder[0].dispose();
                                }
                                return;
                            }
                        } else if ("error".equals(type)) {
                            errored[0] = true;
                            if (!sendJson(emitter, json)) {
                                if (holder[0] != null) {
                                    holder[0].dispose();
                                }
                                return;
                            }
                        } else if ("done".equals(type)) {
                            done[0] = true;
                        }
                    } catch (Exception e) {
                        log.warn("agent 帧解析失败: {}", json, e);
                    }
                },
                ex -> {
                    errored[0] = true;
                    log.error("agent 上游流异常", ex);
                    cb.onError(1, TimeUnit.MILLISECONDS, ex);
                    sendJson(emitter, Map.of("type", "error", "content", MSG_UNAVAILABLE));
                    try {
                        emitter.completeWithError(ex);
                    } catch (Exception ignore) {
                        // 连接已断开，忽略
                    }
                },
                () -> {
                    if (done[0]) {
                        cb.onSuccess(1, TimeUnit.MILLISECONDS);
                    } else {
                        cb.onError(1, TimeUnit.MILLISECONDS, new RuntimeException("agent 流未正常结束(done 缺失)"));
                    }
                    try {
                        emitter.complete();
                    } catch (Exception ignore) {
                        // 连接已断开，忽略
                    }
                }
        );
        holder[0] = disposable;

        // 超时 / 错误时取消上游订阅，避免资源泄漏
        emitter.onTimeout(() -> {
            if (holder[0] != null) {
                holder[0].dispose();
            }
            cb.onError(1, TimeUnit.MILLISECONDS, new TimeoutException(MSG_TIMEOUT));
            try {
                emitter.completeWithError(new TimeoutException(MSG_TIMEOUT));
            } catch (Exception ignore) {
                // 忽略
            }
        });
        emitter.onError(e -> {
            if (holder[0] != null) {
                holder[0].dispose();
            }
        });

        // 流正常结束后落库助手消息（与 done 帧无关，保证会话记录完整）
        emitter.onCompletion(() -> persistAssistant(conversationId, userId, assistantBuf.toString(), errored[0]));

        return emitter;
    }

    @Override
    public List<AgentConversation> listConversations(Long userId) {
        // 千问式侧边栏排序：置顶会话优先（pinned DESC），其次最近活跃（update_time DESC）
        return conversationMapper.selectList(new LambdaQueryWrapper<AgentConversation>()
                .eq(AgentConversation::getUserId, userId)
                .orderByDesc(AgentConversation::getPinned)
                .orderByDesc(AgentConversation::getUpdateTime));
    }

    @Override
    public List<AgentMessage> listMessages(Long userId, String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return Collections.emptyList();
        }
        try {
            Long cid = Long.parseLong(conversationId);
            AgentConversation conv = conversationMapper.selectById(cid);
            if (conv == null || !userId.equals(conv.getUserId())) {
                // 越权或会话不存在 → 视为空列表（前端拿到空数组就清空 UI，不报错打扰用户）
                return Collections.emptyList();
            }
            return messageMapper.selectList(new LambdaQueryWrapper<AgentMessage>()
                    .eq(AgentMessage::getConversationId, cid)
                    .orderByAsc(AgentMessage::getCreateTime));
        } catch (NumberFormatException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public String createConversation(Long userId) {
        AgentConversation conv = new AgentConversation();
        conv.setUserId(userId);
        conv.setTitle("新的对话");
        conv.setPinned(0);
        conv.setGroupId(null);
        conv.setDelFlag(0);
        setAudit(conv, userId);
        conversationMapper.insert(conv);
        return String.valueOf(conv.getId());
    }

    @Override
    public boolean deleteConversation(Long userId, String id) {
        try {
            Long cid = Long.parseLong(id);
            AgentConversation conv = conversationMapper.selectById(cid);
            if (conv == null || !userId.equals(conv.getUserId())) {
                return false;
            }
            // 级联逻辑删除消息
            messageMapper.delete(new LambdaQueryWrapper<AgentMessage>()
                    .eq(AgentMessage::getConversationId, cid));
            conversationMapper.deleteById(cid);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean renameConversation(Long userId, String id, String title) {
        try {
            Long cid = Long.parseLong(id);
            AgentConversation conv = conversationMapper.selectById(cid);
            if (conv == null || !userId.equals(conv.getUserId())) {
                return false;
            }
            conv.setTitle(title.trim());
            conv.setUpdateTime(LocalDateTime.now());
            conv.setUpdateBy(String.valueOf(userId));
            return conversationMapper.updateById(conv) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean clearConversations(Long userId) {
        try {
            List<AgentConversation> convs = conversationMapper.selectList(
                    new LambdaQueryWrapper<AgentConversation>().eq(AgentConversation::getUserId, userId));
            if (convs.isEmpty()) {
                return true;
            }
            List<Long> ids = convs.stream().map(AgentConversation::getId).toList();
            // 级联删除全部消息
            messageMapper.delete(new LambdaQueryWrapper<AgentMessage>()
                    .in(AgentMessage::getConversationId, ids));
            conversationMapper.delete(new LambdaQueryWrapper<AgentConversation>()
                    .in(AgentConversation::getId, ids));
            return true;
        } catch (Exception e) {
            log.error("清空会话失败 userId={}", userId, e);
            return false;
        }
    }

    // ====================== V2.4 千问式侧边栏 ======================

    @Override
    public boolean pinConversation(Long userId, String id, boolean pinned) {
        try {
            Long cid = Long.parseLong(id);
            AgentConversation conv = conversationMapper.selectById(cid);
            if (conv == null || !userId.equals(conv.getUserId())) {
                return false;
            }
            conv.setPinned(pinned ? 1 : 0);
            conv.setUpdateTime(LocalDateTime.now());
            conv.setUpdateBy(String.valueOf(userId));
            return conversationMapper.updateById(conv) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean moveToGroup(Long userId, String id, Long groupId) {
        try {
            Long cid = Long.parseLong(id);
            AgentConversation conv = conversationMapper.selectById(cid);
            if (conv == null || !userId.equals(conv.getUserId())) {
                return false;
            }
            // 若 groupId 非空，须校验分组归属（防止越权把会话移到别人的分组）
            if (groupId != null) {
                AgentConversationGroup grp = groupMapper.selectById(groupId);
                if (grp == null || !userId.equals(grp.getUserId())) {
                    return false;
                }
            }
            // 用 LambdaUpdateWrapper.set 强制把 group_id 写入（含 NULL = 移回最近对话）
            conversationMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<AgentConversation>()
                    .eq(AgentConversation::getId, cid)
                    .eq(AgentConversation::getUserId, userId)
                    .set(AgentConversation::getGroupId, groupId)
                    .set(AgentConversation::getUpdateTime, LocalDateTime.now())
                    .set(AgentConversation::getUpdateBy, String.valueOf(userId)));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public int batchDeleteConversations(Long userId, List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        // 过滤：仅保留本人会话的 id
        List<Long> longIds = ids.stream()
                .map(this::safeParseLong)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (longIds.isEmpty()) {
            return 0;
        }
        List<AgentConversation> owned = conversationMapper.selectList(
                new LambdaQueryWrapper<AgentConversation>()
                        .eq(AgentConversation::getUserId, userId)
                        .in(AgentConversation::getId, longIds));
        if (owned.isEmpty()) {
            return 0;
        }
        List<Long> ownedIds = owned.stream().map(AgentConversation::getId).toList();
        // 级联删除消息
        messageMapper.delete(new LambdaQueryWrapper<AgentMessage>()
                .in(AgentMessage::getConversationId, ownedIds));
        int deleted = conversationMapper.delete(new LambdaQueryWrapper<AgentConversation>()
                .in(AgentConversation::getId, ownedIds));
        return deleted;
    }

    @Override
    public List<AgentConversationGroup> listGroups(Long userId) {
        return groupMapper.selectList(new LambdaQueryWrapper<AgentConversationGroup>()
                .eq(AgentConversationGroup::getUserId, userId)
                .orderByAsc(AgentConversationGroup::getSortOrder)
                .orderByAsc(AgentConversationGroup::getId));
    }

    @Override
    public String createGroup(Long userId, String name, Integer sortOrder) {
        AgentConversationGroup grp = new AgentConversationGroup();
        grp.setUserId(userId);
        grp.setName(name.trim());
        grp.setSortOrder(sortOrder == null ? 0 : sortOrder);
        grp.setDelFlag(0);
        setAudit(grp, userId);
        groupMapper.insert(grp);
        return String.valueOf(grp.getId());
    }

    @Override
    public boolean renameGroup(Long userId, String id, String name) {
        try {
            Long gid = Long.parseLong(id);
            AgentConversationGroup grp = groupMapper.selectById(gid);
            if (grp == null || !userId.equals(grp.getUserId())) {
                return false;
            }
            grp.setName(name.trim());
            grp.setUpdateTime(LocalDateTime.now());
            grp.setUpdateBy(String.valueOf(userId));
            return groupMapper.updateById(grp) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean deleteGroup(Long userId, String id) {
        try {
            Long gid = Long.parseLong(id);
            AgentConversationGroup grp = groupMapper.selectById(gid);
            if (grp == null || !userId.equals(grp.getUserId())) {
                return false;
            }
            // 把该分组下所有会话的 group_id 置 NULL（归回最近对话未分组）。
            // 用 LambdaUpdateWrapper.set(...) 强制把 group_id 写成 NULL，
            // 绕开 MyBatis-Plus 默认 FieldStrategy.NOT_NULL 对 null 字段的跳过。
            conversationMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<AgentConversation>()
                    .eq(AgentConversation::getUserId, userId)
                    .eq(AgentConversation::getGroupId, gid)
                    .set(AgentConversation::getGroupId, null)
                    .set(AgentConversation::getUpdateTime, LocalDateTime.now())
                    .set(AgentConversation::getUpdateBy, String.valueOf(userId)));
            return groupMapper.deleteById(gid) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** 容错地把字符串转 Long，失败返回 null（用于批量过滤） */
    private Long safeParseLong(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ============================ 私有方法 ============================

    private String resolveConversation(Long userId, String conversationId, String firstMessage) {
        if (conversationId != null && !conversationId.isBlank()) {
            try {
                AgentConversation conv = conversationMapper.selectById(Long.parseLong(conversationId));
                if (conv != null && userId.equals(conv.getUserId())) {
                    return conversationId;
                }
            } catch (NumberFormatException ignored) {
                // 非法ID，回退新建
            }
        }
        AgentConversation conv = new AgentConversation();
        conv.setUserId(userId);
        conv.setTitle(truncate(firstMessage, 20));
        conv.setPinned(0);
        conv.setGroupId(null);
        conv.setDelFlag(0);
        setAudit(conv, userId);
        conversationMapper.insert(conv);
        return String.valueOf(conv.getId());
    }

    private void saveUserMessage(String conversationId, Long userId, String message) {
        AgentMessage msg = new AgentMessage();
        msg.setConversationId(Long.parseLong(conversationId));
        msg.setRole("user");
        msg.setContent(message);
        msg.setDelFlag(0);
        setAudit(msg, userId);
        messageMapper.insert(msg);
    }

    private List<Map<String, String>> loadHistory(Long conversationId, int limit) {
        List<AgentMessage> msgs = messageMapper.selectList(new LambdaQueryWrapper<AgentMessage>()
                .eq(AgentMessage::getConversationId, conversationId)
                .orderByDesc(AgentMessage::getCreateTime)
                .last("LIMIT " + limit));
        Collections.reverse(msgs);
        return msgs.stream().map(m -> {
            Map<String, String> item = new LinkedHashMap<>(2);
            item.put("role", m.getRole());
            item.put("content", m.getContent());
            return item;
        }).collect(Collectors.toList());
    }

    private void persistAssistant(String conversationId, Long userId, String content, boolean errored) {
        try {
            String text = (content == null || content.isBlank())
                    ? (errored ? "⚠️ " + MSG_UNAVAILABLE : "")
                    : content;
            AgentMessage msg = new AgentMessage();
            msg.setConversationId(Long.parseLong(conversationId));
            msg.setRole("assistant");
            msg.setContent(text);
            msg.setDelFlag(0);
            setAudit(msg, userId);
            messageMapper.insert(msg);

            AgentConversation conv = conversationMapper.selectById(Long.parseLong(conversationId));
            if (conv != null) {
                conv.setUpdateTime(LocalDateTime.now());
                conv.setUpdateBy(String.valueOf(userId));
                conversationMapper.updateById(conv);
            }
        } catch (Exception e) {
            log.error("落库助手消息失败 conversationId={}", conversationId, e);
        }
    }

    private void setAudit(BaseEntity entity, Long userId) {
        String operator = String.valueOf(userId);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateBy(operator);
        entity.setCreateTime(now);
        entity.setUpdateBy(operator);
        entity.setUpdateTime(now);
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        String trimmed = s.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }

    private boolean sendJson(SseEmitter emitter, Object json) {
        try {
            if (json instanceof String s) {
                emitter.send(s);
            } else {
                emitter.send(JSON.toJSONString(json));
            }
            return true;
        } catch (IOException e) {
            // 客户端已断开
            return false;
        }
    }
}
