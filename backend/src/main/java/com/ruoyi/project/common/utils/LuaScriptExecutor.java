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

    private final RedisTemplate<String, Object> redisTemplate;

    public LuaScriptExecutor(@Qualifier("acgRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
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

            RedisScript<String> script = new DefaultRedisScript<>(gachaDeductStockScript, String.class);

            List<String> keys = List.of(poolStockKey, userPointsKey);
            List<Object> args = List.of(drawCount, singleCost, tenCost);

            String result = redisTemplate.execute(script, keys, args.toArray());

            if ("SUCCESS".equals(result)) {
                return true;
            }
            
            // 解析 Redis 错误信息
            if (result != null && result.startsWith("ERR")) {
                log.warn("Lua脚本执行失败, poolId: {}, userId: {}, error: {}",
                        poolId, userId, result);
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
        redisTemplate.opsForValue().set(key, stock);
        log.info("设置奖池库存, poolId: {}, stock: {}", poolId, stock);
    }

    /**
     * 设置用户积分
     */
    public void setUserPoints(Long userId, int points) {
        String key = "user:points:" + userId;
        redisTemplate.opsForValue().set(key, points);
        log.info("设置用户积分, userId: {}, points: {}", userId, points);
    }

    /**
     * 获取用户积分
     */
    public int getUserPoints(Long userId) {
        String key = "user:points:" + userId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? ((Number) value).intValue() : 0;
    }
}