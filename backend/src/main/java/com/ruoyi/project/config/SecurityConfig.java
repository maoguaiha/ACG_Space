package com.ruoyi.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.exception.BizErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Spring Security 安全配置
 * <p>
 * 面试亮点：
 * 1. 基于 JWT 的无状态认证（Stateless）
 * 2. BCrypt 密码加密
 * 3. RBAC 权限模型：公开接口放行，业务接口需认证，管理接口需 ADMIN 角色
 * 4. 防 CSRF 攻击（前后端分离场景关闭即可，JWT Bearer 天然防 CSRF）
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // 启用方法级注解 @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    /**
     * BCrypt 密码编码器 — 不可逆加密，面试必问的密码安全实现
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean("securityCorsSource")
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("http://127.0.0.1:*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean("acgSecurityFilterChain")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 前后端分离 + JWT 无状态，关闭 CSRF
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 无状态会话 — JWT 本身就是会话载体
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ===== 公开接口（无需认证） =====
                // 认证相关
                .requestMatchers("/api/auth/**").permitAll()
                // 番剧查询（公开内容）
                .requestMatchers(HttpMethod.GET, "/api/anime/**").permitAll()
                .requestMatchers("/api/anime/bangumi/**", "/api/anime/bgm/**").permitAll()
                // 评论查看
                .requestMatchers(HttpMethod.GET, "/api/comment/page").permitAll()
                // 文章查看
                .requestMatchers(HttpMethod.GET, "/api/article/**").permitAll()
                // 用户公开资料
                .requestMatchers(HttpMethod.GET, "/api/user/*/profile", "/api/user/search").permitAll()
                // Swagger/OpenAPI 文档
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/doc.html").permitAll()
                // Spring 错误页面
                .requestMatchers("/error").permitAll()

                // ===== 管理端接口（需 ADMIN 角色） =====
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // ===== 业务接口（需登录认证） =====
                .anyRequest().authenticated()
            )
            // 禁用 HTTP Basic 和 Form 登录（使用 JWT）
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            // JWT 过滤器在 UsernamePasswordAuthenticationFilter 之前执行
            .addFilterBefore(jwtAuthenticationTokenFilter,
                    org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            // 未认证/未授权时返回 JSON（而非重定向到登录页）
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    ObjectMapper mapper = new ObjectMapper();
                    response.getWriter().write(mapper.writeValueAsString(
                            Result.error(BizErrorCode.UNAUTHORIZED)));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    ObjectMapper mapper = new ObjectMapper();
                    response.getWriter().write(mapper.writeValueAsString(
                            Result.error(BizErrorCode.FORBIDDEN)));
                })
            );

        return http.build();
    }
}
