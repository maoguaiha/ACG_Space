package com.ruoyi.project.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性注解
 * 用于防止重复提交
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 幂等键的前缀，默认为方法名
     */
    String prefix() default "";

    /**
     * 过期时间，默认5秒
     */
    int expireTime() default 5;

    /**
     * 时间单位，默认秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 提示信息
     */
    String message() default "操作过于频繁，请稍后再试";
}