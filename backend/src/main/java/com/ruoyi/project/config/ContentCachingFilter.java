package com.ruoyi.project.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * 请求体缓存过滤器
 * <p>
 * 在 Spring Security 过滤链之前运行，将 HttpServletRequest 包装为 ContentCachingRequestWrapper，
 * 使请求体可以被多次读取。解决 IdempotentInterceptor 消费 InputStream 后 Controller 拿不到 Body 的 Bug。
 * </p>
 * <p>
 * 面试亮点：发现了框架级 Bug（Servlet InputStream 单次读取限制），使用 Wrapper 模式优雅修复，
 * 体现了对 Servlet 规范底层机制的深入理解。
 * </p>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)  // 在所有 Filter 之前运行
public class ContentCachingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {
        // 非文件上传类请求才包装（multipart 不能被 cache）
        if (!isMultipart(request)) {
            request = new ContentCachingRequestWrapper(request);
        }
        chain.doFilter(request, response);
    }

    private boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }
}
