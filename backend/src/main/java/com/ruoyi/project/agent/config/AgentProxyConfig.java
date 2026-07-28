package com.ruoyi.project.agent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Agent 代理配置：构建指向 python-agent 内网地址的 WebClient（Spring 显式命名 Bean，避免冲突）。
 * <p>
 * 注：本项目为 Servlet(WebMvc) 应用，引入 spring-boot-starter-webflux 仅用于获取 WebClient 客户端能力，
 * 不会启动 Netty 服务器（WebApplicationType 仍为 SERVLET）。
 */
@Configuration
public class AgentProxyConfig {

    @Value("${agent.api.base-url:http://localhost:8000}")
    private String baseUrl;

    @Bean("agentWebClient")
    public WebClient agentWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
