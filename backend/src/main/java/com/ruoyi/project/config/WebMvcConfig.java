package com.ruoyi.project.config;

import com.ruoyi.project.common.interceptor.IdempotentInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * <p>
 * IdempotentInterceptor 已修复（配合 ContentCachingFilter 解决 InputStream 消费 Bug），
 * 现正式启用幂等性保护：所有标注 @Idempotent 的 POST 写入接口均受防重复提交保护。
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final IdempotentInterceptor idempotentInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(idempotentInterceptor)
                .addPathPatterns("/api/**");
    }
}