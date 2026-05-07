package com.ruoyi.project.common.aspect;

import com.ruoyi.project.common.annotation.RateLimiterAndCircuitBreaker;
import com.ruoyi.project.common.api.Result;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeoutException;

/**
 * 限流熔断切面
 */
@Slf4j
@Aspect
@Component
public class RateLimiterAndCircuitBreakerAspect {

    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Around("@annotation(annotation)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimiterAndCircuitBreaker annotation) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RateLimiter rateLimiter = null;
        if (!annotation.rateLimiterName().isEmpty()) {
            rateLimiter = rateLimiterRegistry.rateLimiter(annotation.rateLimiterName());
        }

        CircuitBreaker circuitBreaker = null;
        if (!annotation.circuitBreakerName().isEmpty()) {
            circuitBreaker = circuitBreakerRegistry.circuitBreaker(annotation.circuitBreakerName());
        }

        try {
            if (rateLimiter != null && !rateLimiter.acquirePermission()) {
                log.warn("Rate limit exceeded for: {}", method.getName());
                return Result.error(429, "请求过于频繁，请稍后再试");
            }

            if (circuitBreaker != null) {
                return CircuitBreaker.decorateSupplier(circuitBreaker, () -> {
                    try {
                        return joinPoint.proceed();
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }).get();
            }

            return joinPoint.proceed();
        } catch (Exception e) {
            log.error("Exception in rate limiter/circuit breaker: {}", e.getMessage());
            if (e.getCause() instanceof TimeoutException) {
                return Result.error(504, "请求超时，请稍后再试");
            }
            throw e;
        }
    }
}
