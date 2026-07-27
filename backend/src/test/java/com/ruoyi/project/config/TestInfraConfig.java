package com.ruoyi.project.config;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 测试专用基础设施配置。
 *
 * <p>在 test profile 下，RedisConfig / RedissonConfig 已通过 {@code @Profile("!test")} 排除，
 * RocketMQ 自动配置也被排除（见 application-test.yml）。本配置提供一组 mock Bean，
 * 仅用于满足 Spring 上下文装配（IdempotentInterceptor / LuaScriptExecutor /
 * BizAnimeServiceImpl / BizSynthesizeServiceImpl / BizItemServiceImpl /
 * BizCommentServiceImpl 等依赖这些类型），不连接任何真实中间件。</p>
 *
 * <p>关键点：RedisTemplate 的 {@code opsForXxx()} 默认返回 {@code null}，而
 * {@link com.ruoyi.project.common.utils.CacheInitializer}（ApplicationRunner）在启动期、
 * 业务方法在运行期都会调用 {@code redisTemplate.opsForValue().get/set(...)}，若不 stub 会直接 NPE。
 * 因此这里让每个 mock 的 {@code opsForXxx()} 返回可用的子 mock，所有读写都变成安全的空操作。</p>
 *
 * <p>这些测试是 DB 流程测试，运行期不会断言上述 mock Bean 的真实行为。</p>
 */
@Configuration
public class TestInfraConfig {

    @Bean("acgRedisTemplate")
    public RedisTemplate<String, Object> acgRedisTemplate() {
        return mockRedisTemplate(RedisTemplate.class, new InMemoryValueOperations<String, Object>());
    }

    @Bean("luaRedisTemplate")
    public RedisTemplate<String, String> luaRedisTemplate() {
        return mockRedisTemplate(RedisTemplate.class, new InMemoryValueOperations<String, String>());
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        return mockRedisTemplate(StringRedisTemplate.class, new InMemoryValueOperations<String, String>());
    }

    @Bean
    public RedissonClient redissonClient() {
        return mock(RedissonClient.class);
    }

    @Bean
    public RocketMQTemplate rocketMQTemplate() {
        return mock(RocketMQTemplate.class);
    }

    /**
     * 构建一个 RedisTemplate / StringRedisTemplate mock，并 stub 常用访问器，
     * 避免 opsForXxx() 返回 null 导致 NPE。
     *
     * <p>{@code opsForValue()} 直接返回传入的有状态内存实现 {@link InMemoryValueOperations}，
     * 以支撑积分等字符串状态在测试中正确累加（不可用 Mockito mock 替代，原因见该类的注释）。</p>
     *
     * <p>其余 ops 访问器返回子 mock（当前测试流程不依赖其状态）。</p>
     *
     * <p>必须按实际类型 mock（传入 {@code StringRedisTemplate.class} 而非统一 mock 父类
     * {@code RedisTemplate}），否则返回的代理对象无法向下转型为 StringRedisTemplate，
     * Spring 实例化 bean 时会抛出 ClassCastException。</p>
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends RedisTemplate<?, ?>> T mockRedisTemplate(Class<T> clazz,
                                                                       ValueOperations vo) {
        T tpl = mock(clazz);

        when(tpl.opsForValue()).thenReturn(vo);

        // 其余 ops 访问器返回子 mock（当前测试流程不依赖其状态）
        when(tpl.opsForHash()).thenReturn(mock(HashOperations.class));
        when(tpl.opsForList()).thenReturn(mock(ListOperations.class));
        when(tpl.opsForSet()).thenReturn(mock(SetOperations.class));
        when(tpl.opsForZSet()).thenReturn(mock(ZSetOperations.class));

        // 常用 key 级操作的安全默认返回值
        when(tpl.hasKey(any())).thenReturn(false);

        return tpl;
    }
}
