package com.ruoyi.project.common.interceptor;

import com.ruoyi.project.common.annotation.Idempotent;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 幂等性拦截器
 * 基于Redis实现防重复提交
 */
@Slf4j
@Component
public class IdempotentInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String IDEMPOTENT_KEY_PREFIX = "idempotent:";

    public IdempotentInterceptor(@Qualifier("acgRedisTemplate") RedisTemplate<String, Object> redisTemplate,
                                  ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        java.lang.reflect.Method method = handlerMethod.getMethod();
        Idempotent idempotent = method.getAnnotation(Idempotent.class);

        if (idempotent == null) {
            return true;
        }

        try {
            // 获取幂等键
            String key = buildIdempotentKey(request, idempotent);

            // 尝试获取锁
            Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1",
                    idempotent.expireTime(), idempotent.timeUnit());

            if (success == null || !success) {
                // 重复请求
                log.warn("重复请求被拦截, key: {}", key);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(Result.error(idempotent.message())));
                return false;
            }
        } catch (Exception e) {
            log.error("幂等性检查异常, 放行请求: {}", e.getMessage());
            // 发生异常时放行，避免阻塞业务
            return true;
        }

        return true;
    }

    /**
     * 构建幂等键
     */
    private String buildIdempotentKey(HttpServletRequest request, Idempotent idempotent) {
        StringBuilder key = new StringBuilder(IDEMPOTENT_KEY_PREFIX);

        // 添加前缀（方法名或自定义前缀）
        String prefix = idempotent.prefix();
        if (prefix.isEmpty()) {
            // 默认使用请求路径作为前缀
            prefix = request.getRequestURI();
        }
        key.append(prefix).append(":");

        // 添加用户ID（如果已登录）
        try {
            Long userId = SecurityUtils.getUserId();
            if (userId != null) {
                key.append("user:").append(userId).append(":");
            }
        } catch (Exception e) {
            log.debug("获取用户ID失败，使用匿名用户标识: {}", e.getMessage());
            key.append("anonymous:");
        }

        // 添加请求参数的哈希（用于区分不同参数的请求）
        String params = request.getQueryString();
        if (params != null && !params.isEmpty()) {
            key.append("query:").append(params.hashCode());
        }

        // 添加请求体的哈希（支持 POST 请求）
        String body = getRequestBody(request);
        if (body != null && !body.isEmpty()) {
            key.append("body:").append(body.hashCode());
        }

        return key.toString();
    }

    /**
     * 获取请求体内容
     */
    private String getRequestBody(HttpServletRequest request) {
        try {
            request.setCharacterEncoding("UTF-8");
            // 读取请求体（使用 ByteArrayInputStream 包装以支持重复读取）
            byte[] bodyBytes = request.getInputStream().readAllBytes();
            if (bodyBytes.length > 0) {
                String body = new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);
                // 将读取的内容重新设置到请求中，以便后续过滤器/控制器继续使用
                request.setAttribute("requestBody", body);
                return body;
            }
        } catch (Exception e) {
            log.debug("读取请求体失败: {}", e.getMessage());
        }
        return null;
    }
}