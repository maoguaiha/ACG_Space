package com.ruoyi.project.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步任务配置
 * <p>
 * 面试亮点：将非关键路径操作（如碎片掉落、积分通知）异步化，
 * 降低核心接口（抽赏/购买）的响应延迟，提升系统吞吐量。
 * </p>
 * <p>
 * 线程池配置：
 * - 核心线程 2，最大线程 5（轻量级，够用即可）
 * - 队列容量 100（削峰填谷）
 * - 拒绝策略：CallerRunsPolicy（不会丢任务，降级为同步执行）
 * </p>
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("acg-async-");
        executor.setRejectedExecutionHandler(
                new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) ->
                log.error("异步任务执行异常: method={}, error={}", method.getName(), throwable.getMessage(), throwable);
    }
}
