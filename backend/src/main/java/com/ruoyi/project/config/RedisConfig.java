package com.ruoyi.project.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Redis 配置类
 * 强制指定 Bean 名称，防止与 Spring Boot 默认配置冲突 (BeanDefinitionOverrideException)
 *
 * 注意：此处使用内联的 Fastjson2RedisSerializer，避免依赖 fastjson2-extension-spring6
 * 中路径不稳定的 FastJson2JsonRedisSerializer，仅使用 fastjson2 核心包 API。
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean("acgRedisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用内联的 Fastjson2 序列化器（仅依赖 fastjson2 核心包）
        Fastjson2RedisSerializer fastjson2RedisSerializer = new Fastjson2RedisSerializer();

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // key 采用 String 序列化
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // value 采用 Fastjson2 序列化
        template.setValueSerializer(fastjson2RedisSerializer);
        template.setHashValueSerializer(fastjson2RedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 专门用于 Lua 脚本执行的 Redis 模板
     * 使用纯 String 序列化，避免 Fastjson2 反序列化纯字符串失败
     */
    @Bean("luaRedisTemplate")
    public RedisTemplate<String, String> luaRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // 全部使用 String 序列化
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setValueSerializer(stringRedisSerializer);
        template.setHashValueSerializer(stringRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis 缓存管理器 — 配置 TTL 防止缓存雪崩
     * <p>
     * activePools: 10 分钟 TTL（奖池变更有 @CacheEvict 主动清除）
     * 默认: 5 分钟 TTL
     * </p>
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Fastjson2RedisSerializer serializer = new Fastjson2RedisSerializer();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        RedisCacheConfiguration activePoolsConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("activePools", activePoolsConfig)
                .build();
    }

    /**
     * 内联 Fastjson2 Redis 序列化器
     * 直接使用 fastjson2 核心包的 JSON.toJSONBytes / JSON.parseObject，
     * 无需依赖任何扩展包，兼容性最佳。
     * 序列化时写入类型信息，反序列化时自动还原对象类型。
     */
    static class Fastjson2RedisSerializer implements RedisSerializer<Object> {

        private static final String ALLOWED_PACKAGE_PREFIX = "com.ruoyi.project.";

        @Override
        public byte[] serialize(Object object) throws SerializationException {
            if (object == null) {
                return new byte[0];
            }
            try {
                return JSON.toJSONBytes(object, JSONWriter.Feature.WriteClassName);
            } catch (Exception e) {
                throw new SerializationException("Fastjson2 序列化失败: " + e.getMessage(), e);
            }
        }

        @Override
        @SuppressWarnings("deprecation")
        public Object deserialize(byte[] bytes) throws SerializationException {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            try {
                return JSON.parseObject(
                        new String(bytes, StandardCharsets.UTF_8),
                        Object.class,
                        JSONReader.Feature.SupportAutoType
                );
            } catch (Exception e) {
                throw new SerializationException("Fastjson2 反序列化失败: " + e.getMessage(), e);
            }
        }
    }
}

