package com.ruoyi.project.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI (Swagger 3) 配置
 * <p>
 * 访问地址:
 * - Swagger UI: http://localhost:8080/swagger-ui/index.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * </p>
 * <p>
 * 面试亮点：规范的 API 文档是工程化项目的标志，
 * 面试官可以直接在浏览器中浏览和测试所有接口。
 * </p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI acgSpaceOpenAPI() {
        // JWT Bearer 认证配置 — 在 Swagger UI 中可以方便地填入 Token 测试
        SecurityScheme jwtScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("Authorization")
                .description("登录接口获取的 JWT Token，格式: Bearer {token}");

        return new OpenAPI()
                .info(new Info()
                        .title("ACG Space API 文档")
                        .description("ACG Space — 二次元社区·抽赏·市场交易平台 API\n\n"
                                + "**核心功能模块：**\n"
                                + "- 用户认证：登录/注册/JWT Token\n"
                                + "- 抽赏系统：奖池管理、抽赏(Lua原子扣减)、保底机制\n"
                                + "- 市场交易：上架/购买/下架（含1%手续费）\n"
                                + "- 资产系统：资产查看、碎片合成、积分兑换\n"
                                + "- 社区互动：文章、评论、番剧、私信\n\n"
                                + "**技术架构：** Spring Boot 3.2 + MyBatis-Plus + Redis/Redisson + RocketMQ + Resilience4j")
                        .version("2.2.0")
                        .contact(new Contact()
                                .name("ACG Space Team")
                                .email("dev@acgspace.dev"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .components(new Components().addSecuritySchemes("Bearer", jwtScheme));
    }
}
