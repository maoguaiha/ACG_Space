package com.ruoyi.project.config;

import com.ruoyi.project.common.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * JWT 认证过滤器
 * 
 * 性能优化：shouldNotFilter 跳过公开接口，减少无效 JWT 解析开销
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);

    /** 无需 Token 校验的公开路径前缀（GET 方法生效） */
    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/api/anime", "/api/comment/page",
            "/api/article", "/swagger-ui", "/v3/api-docs", "/error", "/health"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 如果请求带了 Authorization header，走正常解析流程（不跳过）
        // 这样公开 GET 接口也能识别当前登录用户（如 reaction-status）
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return false;
        }

        // GET 请求的公开路径不需要 Token 校验
        if ("GET".equalsIgnoreCase(method)) {
            return PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
        }
        // POST /api/auth/** 也不需要
        return path.startsWith("/api/auth/");
    }

    /**
     * 异步 dispatch 时不再重新执行 JWT 鉴权。
     * <p>
     * 背景：/api/agent/chat 是 SseEmitter 异步流。controller 返回后 response
     * 立即 committed（SSE 已开始吐数据），随后 Tomcat 以异步线程继续写流；
     * 流结束/超时/客户端断开时会触发一次 ASYNC dispatch，过滤器链再次执行。
     * 此时 SecurityContextHolder（ThreadLocal）在主线程解析的认证上下文不
     * 会传播到异步线程，若在此重新鉴权会抛 AccessDeniedException，且因
     * response 已 committed 无法写入 403 —— 日志表现为：
     *   "Unable to handle the Spring Security Exception because the response
     *    is already committed."
     * 修复：ASYNC dispatch 跳过 JWT 解析（认证已在首次请求完成，无需重复）。
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                Long userId = jwtUtils.getUserId(token);
                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userId, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (Exception e) {
                // Token 无效或过期，不设置上下文，后续过滤器会处理权限
                log.warn("JWT 校验失败 path={} tokenPrefix={} err={}",
                        request.getRequestURI(),
                        token.length() > 14 ? token.substring(0, 14) : token,
                        e.toString());
            }
        }
        chain.doFilter(request, response);
    }
}
