package com.ruoyi.project.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;

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
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
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
     * 内联 Fastjson2 Redis 序列化器
     * 直接使用 fastjson2 核心包的 JSON.toJSONBytes / JSON.parseObject，
     * 无需依赖任何扩展包，兼容性最佳。
     * 序列化时写入类型信息，反序列化时自动还原对象类型。
     */
    static class Fastjson2RedisSerializer implements RedisSerializer<Object> {

        @Override
        public byte[] serialize(Object object) throws SerializationException {
            if (object == null) {
                return new byte[0];
            }
            try {
                // 写入 @type 字段，反序列化时可还原具体类型
                return JSON.toJSONBytes(object, JSONWriter.Feature.WriteClassName);
            } catch (Exception e) {
                throw new SerializationException("Fastjson2 序列化失败: " + e.getMessage(), e);
            }
        }

        @Override
        @SuppressWarnings("deprecation") // SupportAutoType 在 fastjson2 中已废弃，但目前无等价的无配置替代方案
        public Object deserialize(byte[] bytes) throws SerializationException {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            try {
                // SupportAutoType 用于还原 @type 字段所记录的具体类型
                // 已知废弃：未来版本可通过 JSONFactory.getDefaultObjectReaderProvider()
                //           .addAutoTypeAccept("com.ruoyi.project.") 替代
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

