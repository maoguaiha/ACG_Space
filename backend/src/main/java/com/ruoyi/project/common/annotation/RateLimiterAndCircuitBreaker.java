package com.ruoyi.project.common.annotation;

import java.lang.annotation.*;

/**
 * 限流熔断注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiterAndCircuitBreaker {

    /**
     * 限流器名称
     */
    String rateLimiterName() default "";

    /**
     * 熔断器名称
     */
    String circuitBreakerName() default "";
}
