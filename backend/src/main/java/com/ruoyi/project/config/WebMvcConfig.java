package com.ruoyi.project.config;

import com.ruoyi.project.common.interceptor.IdempotentInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final IdempotentInterceptor idempotentInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 暂时禁用幂等性拦截器以排查问题
        // registry.addInterceptor(idempotentInterceptor)
        //         .addPathPatterns("/api/**");
    }
}