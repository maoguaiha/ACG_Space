-- ==============================================
-- 获取奖池库存 Lua 脚本
-- ==============================================

-- 参数说明
-- KEYS[1]: 奖池库存Key (gacha:pool:{poolId}:stock)

local poolStockKey = KEYS[1]
local stock = redis.call('GET', poolStockKey)

if not stock then
    return 0
end

return tonumber(stock)