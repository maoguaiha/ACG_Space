package com.ruoyi.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 全局跨域配置
 * 允许前台 (Nuxt3 开发端口 3000) 和管理端 (Vite 开发端口 5173) 访问后端 API
 */
@Configuration
public class CorsConfig {

    @Bean("globalCorsFilter")
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许携带 Cookie（登录态）
        config.setAllowCredentials(true);
        // 允许的来源（开发阶段放开前台和管理端两个端口）
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://127.0.0.1:3000");
        config.addAllowedOrigin("http://127.0.0.1:5173");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有 HTTP 方法
        config.addAllowedMethod("*");
        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有 /api/** 路径生效
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
