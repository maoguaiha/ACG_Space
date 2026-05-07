package com.ruoyi.project.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/risk-control")
@RequiredArgsConstructor
public class AdminRiskControlController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;

    @GetMapping("/circuit-breakers")
    public List<CircuitBreakerVO> getCircuitBreakers() {
        return circuitBreakerRegistry.getAllCircuitBreakers()
                .stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    @GetMapping("/rate-limiters")
    public List<RateLimiterVO> getRateLimiters() {
        return rateLimiterRegistry.getAllRateLimiters()
                .stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    @GetMapping("/overview")
    public RiskOverviewVO getOverview() {
        RiskOverviewVO overview = new RiskOverviewVO();
        overview.setCircuitBreakers(getCircuitBreakers());
        overview.setRateLimiters(getRateLimiters());
        return overview;
    }

    private CircuitBreakerVO toVo(CircuitBreaker circuitBreaker) {
        CircuitBreakerVO vo = new CircuitBreakerVO();
        vo.setName(circuitBreaker.getName());
        vo.setState(circuitBreaker.getState().name());
        vo.setFailureRate(circuitBreaker.getMetrics().getFailureRate());
        vo.setNumberOfBufferedCalls(circuitBreaker.getMetrics().getNumberOfBufferedCalls());
        vo.setNumberOfFailedCalls(circuitBreaker.getMetrics().getNumberOfFailedCalls());
        vo.setNumberOfSuccessfulCalls(circuitBreaker.getMetrics().getNumberOfSuccessfulCalls());
        return vo;
    }

    private RateLimiterVO toVo(RateLimiter rateLimiter) {
        RateLimiterVO vo = new RateLimiterVO();
        vo.setName(rateLimiter.getName());
        vo.setAvailablePermissions(rateLimiter.getMetrics().getAvailablePermissions());
        vo.setNumberOfWaitingThreads(rateLimiter.getMetrics().getNumberOfWaitingThreads());
        return vo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskOverviewVO {
        private List<CircuitBreakerVO> circuitBreakers;
        private List<RateLimiterVO> rateLimiters;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CircuitBreakerVO {
        private String name;
        private String state;
        private Float failureRate;
        private Integer numberOfBufferedCalls;
        private Integer numberOfFailedCalls;
        private Integer numberOfSuccessfulCalls;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLimiterVO {
        private String name;
        private Integer availablePermissions;
        private Integer numberOfWaitingThreads;
    }
}
