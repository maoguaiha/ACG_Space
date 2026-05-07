-- ==============================================
-- 抽赏库存扣减 Lua 脚本
-- 实现原子化的库存校验与扣减，防止超卖
-- ==============================================

-- 参数说明
-- KEYS[1]: 奖池库存Key (gacha:pool:{poolId}:stock)
-- KEYS[2]: 用户积分Key (user:points:{userId})
-- ARGV[1]: 抽赏次数 (1或10)
-- ARGV[2]: 单次消耗积分
-- ARGV[3]: 十连消耗积分

-- 获取参数
local poolStockKey = KEYS[1]
local userPointsKey = KEYS[2]
local drawCount = tonumber(ARGV[1])
local singleCost = tonumber(ARGV[2])
local tenCost = tonumber(ARGV[3])

-- 计算消耗积分
local cost = drawCount == 10 and tenCost or singleCost

-- 获取当前库存
local currentStock = tonumber(redis.call('GET', poolStockKey))
if not currentStock or currentStock <= 0 then
    return redis.error_reply('库存不足')
end

-- 校验库存是否足够
if currentStock < drawCount then
    return redis.error_reply('剩余库存不足，当前库存: ' .. currentStock .. '，需要: ' .. drawCount)
end

-- 获取用户积分
local userPoints = tonumber(redis.call('GET', userPointsKey))
if not userPoints or userPoints < cost then
    return redis.error_reply('积分不足')
end

-- 原子扣减库存
redis.call('DECRBY', poolStockKey, drawCount)

-- 原子扣减积分
redis.call('DECRBY', userPointsKey, cost)

-- 返回成功结果
return redis.status_reply('SUCCESS')