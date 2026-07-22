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
import java.util.ArrayList;
import java.util.Set;

/**
 * JWT 认证过滤器
 * 
 * 性能优化：shouldNotFilter 跳过公开接口，减少无效 JWT 解析开销
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    /** 无需 Token 校验的公开路径前缀 */
    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/api/anime", "/api/auth", "/api/comment/page",
            "/api/article", "/api/user", "/swagger-ui", "/v3/api-docs", "/error"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        // GET 请求的公开路径不需要 Token 校验
        if ("GET".equalsIgnoreCase(method)) {
            return PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
        }
        // POST /api/auth/** 也不需要
        return path.startsWith("/api/auth/");
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
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (Exception e) {
                // Token 无效或过期，不设置上下文，后续过滤器会处理权限
            }
        }
        chain.doFilter(request, response);
    }
}
