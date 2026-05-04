package com.ruoyi.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security 安全配置
 * 遵循 RuoYi 风格并适配 Spring Boot 3
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * 提供 CorsConfigurationSource 供 Security 的 CORS 过滤器使用
     * 确保 DELETE / PUT 等非简单请求的 OPTIONS 预检能正常通过
     */
    @Bean("securityCorsSource")
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://127.0.0.1:3000");
        config.addAllowedOrigin("http://127.0.0.1:5173");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Bean("acgSecurityFilterChain")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF (前后端分离项目通常禁用，改用 JWT 防御)
            .csrf(AbstractHttpConfigurer::disable)
            // 使用上面定义的 CorsConfigurationSource，确保 OPTIONS 预检请求不被拦截
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 请求权限配置
            .authorizeHttpRequests(auth -> auth
                // 放开认证、首页、番剧查询、每日放送等公共接口
                .requestMatchers("/api/auth/**", "/api/anime/bangumi/**", "/api/anime/bgm/**", "/api/follow/status/bangumi/**").permitAll()
                .requestMatchers("/api/anime/list", "/api/anime/page", "/api/anime/library/**", "/api/anime/featured", "/api/anime/calendar", "/api/anime/{id}").permitAll()
                .requestMatchers("/api/comment/page").permitAll()
                .requestMatchers("/api/article/**", "/api/comment/**", "/api/follow/list").permitAll()
                .requestMatchers("/api/user/*/profile", "/api/user/search", "/api/user/follow/status").permitAll()
                .requestMatchers("/error").permitAll()
                // 其他请求（如追番、评论、管理端同步等）需要认证
                .anyRequest().authenticated()
            )
            // 禁用 HTTP Basic 弹窗
            .httpBasic(AbstractHttpConfigurer::disable)
            // 禁用表单登录
            .formLogin(AbstractHttpConfigurer::disable)
            // 添加 JWT 过滤器
            .addFilterBefore(jwtAuthenticationTokenFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

