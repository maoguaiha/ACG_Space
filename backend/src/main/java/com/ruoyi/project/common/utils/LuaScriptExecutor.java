package com.ruoyi.project.common.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Lua脚本执行器
 * 用于执行Redis Lua脚本，实现原子化操作
 */
@Slf4j
@Component
public class LuaScriptExecutor {

    private final RedisTemplate<String, String> redisTemplate;

    public LuaScriptExecutor(
            @Qualifier("luaRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        log.info("LuaScriptExecutor 初始化完成，使用 luaRedisTemplate 执行 Lua 脚本");
    }

    private String gachaDeductStockScript;
    private String gachaGetStockScript;

    @PostConstruct
    public void init() {
        try {
            gachaDeductStockScript = loadScript("lua/gacha_deduct_stock.lua");
            gachaGetStockScript = loadScript("lua/gacha_get_stock.lua");
            log.info("Lua脚本加载完成");
        } catch (IOException e) {
            log.error("加载Lua脚本失败", e);
            throw new RuntimeException("加载Lua脚本失败", e);
        }
    }

    /**
     * 从classpath加载Lua脚本
     */
    private String loadScript(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 执行抽赏库存扣减脚本
     *
     * @param poolId     奖池ID
     * @param userId     用户ID
     * @param drawCount  抽赏次数(1或10)
     * @param singleCost 单抽消耗积分
     * @param tenCost    十连消耗积分
     * @return true-成功, false-失败
     */
    public boolean executeGachaDeduct(Long poolId, Long userId, int drawCount,
                                      int singleCost, int tenCost) {
        try {
            String poolStockKey = "gacha:pool:" + poolId + ":stock";
            String userPointsKey = "user:points:" + userId;

            // 使用 acgRedisTemplate 执行，Fastjson2 序列化器会自动处理类型转换
            RedisScript<Object> script = new DefaultRedisScript<>(gachaDeductStockScript, Object.class);

            List<String> keys = List.of(poolStockKey, userPointsKey);
            // Lua 脚本参数必须是字符串类型，需要显式转换
            List<String> args = List.of(String.valueOf(drawCount), String.valueOf(singleCost), String.valueOf(tenCost));

            // 使用 redisTemplate 执行
            Object result;
            try {
                result = redisTemplate.execute(script, keys, args.toArray());
            } catch (Exception e) {
                log.error("Lua 脚本执行异常，poolId: {}, userId: {}, error: {}", 
                        poolId, userId, e.getMessage());
                return false;
            }

            log.info("Lua 脚本执行结果，poolId: {}, userId: {}, result: {}, type: {}", 
                    poolId, userId, result, result != null ? result.getClass().getSimpleName() : "null");

            // 判断是否成功：SUCCESS 字符串或 1 (Long)
            if (result != null) {
                String resultStr = result.toString();
                if ("SUCCESS".equals(resultStr) || "1".equals(resultStr)) {
                    return true;
                }
                if (resultStr.startsWith("ERR")) {
                    log.warn("Lua 脚本执行失败，poolId: {}, userId: {}, error: {}",
                            poolId, userId, resultStr);
                }
            }
            
            return false;
        } catch (Exception e) {
            log.error("执行抽赏扣减脚本失败, poolId: {}, userId: {}, error: {}",
                    poolId, userId, e.getMessage());
            return false;
        }
    }

    /**
     * 获取最后一次 Lua 脚本执行的错误信息
     */
    private String lastLuaErrorMessage;

    public String getLastLuaErrorMessage() {
        return lastLuaErrorMessage;
    }

    /**
     * 获取奖池当前库存
     */
    public int getPoolStock(Long poolId) {
        try {
            String poolStockKey = "gacha:pool:" + poolId + ":stock";

            RedisScript<Long> script = new DefaultRedisScript<>(gachaGetStockScript, Long.class);

            Long result = redisTemplate.execute(script, Collections.singletonList(poolStockKey));
            return result != null ? result.intValue() : 0;
        } catch (Exception e) {
            log.error("获取奖池库存失败, poolId: {}", poolId, e);
            return 0;
        }
    }

    /**
     * 设置奖池库存（初始化时使用）
     */
    public void setPoolStock(Long poolId, int stock) {
        String key = "gacha:pool:" + poolId + ":stock";
        redisTemplate.opsForValue().set(key, String.valueOf(stock));
        log.info("设置奖池库存, poolId: {}, stock: {}", poolId, stock);
    }

    /**
     * 设置用户积分
     */
    public void setUserPoints(Long userId, int points) {
        String key = "user:points:" + userId;
        redisTemplate.opsForValue().set(key, String.valueOf(points));
        log.info("设置用户积分, userId: {}, points: {}", userId, points);
    }

    /**
     * 获取用户积分
     */
    public int getUserPoints(Long userId) {
        String key = "user:points:" + userId;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null || value.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.error("解析用户积分失败, userId: {}, value: {}", userId, value, e);
            return 0;
        }
    }

    /**
     * 扣除用户积分
     */
    public void deductUserPoints(Long userId, int points) {
        String key = "user:points:" + userId;
        String value = redisTemplate.opsForValue().get(key);
        int current = 0;
        if (value != null && !value.isEmpty()) {
            try {
                current = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                log.error("解析用户积分失败, userId: {}, value: {}", userId, value, e);
            }
        }
        int newPoints = Math.max(0, current - points);
        redisTemplate.opsForValue().set(key, String.valueOf(newPoints));
        log.info("扣除用户积分, userId: {}, 扣除: {}, 剩余: {}", userId, points, newPoints);
    }

    /**
     * 尝试获取分布式锁
     * @param lockKey 锁的 key
     * @param expireSeconds 锁的过期时间（秒）
     * @return true=获取成功，false=获取失败
     */
    public Boolean tryLock(String lockKey, long expireSeconds) {
        String script = 
            "if redis.call('SETNX', KEYS[1], ARGV[1]) == 1 then " +
            "    redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2])) " +
            "    return 1 " +
            "else " +
            "    return 0 " +
            "end";
        
        RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = redisTemplate.execute(
            redisScript, 
            Collections.singletonList(lockKey), 
            "locked:" + System.currentTimeMillis(), 
            String.valueOf(expireSeconds)
        );
        
        if (result != null && result == 1) {
            log.debug("获取锁成功，key: {}", lockKey);
            return true;
        } else {
            log.debug("获取锁失败，key: {}", lockKey);
            return false;
        }
    }

    /**
     * 释放分布式锁
     * @param lockKey 锁的 key
     */
    public void unlock(String lockKey) {
        String script = 
            "if redis.call('GET', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('DEL', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";
        
        // 简单删除锁（生产环境建议使用更安全的解锁方式）
        redisTemplate.delete(lockKey);
        log.debug("释放锁，key: {}", lockKey);
    }
}