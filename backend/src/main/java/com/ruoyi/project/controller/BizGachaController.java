package com.ruoyi.project.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.project.common.annotation.Idempotent;
import com.ruoyi.project.common.annotation.RateLimiterAndCircuitBreaker;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.LuaScriptExecutor;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.dto.GachaPrizeDTO;
import com.ruoyi.project.domain.entity.BizGachaPool;
import com.ruoyi.project.domain.entity.BizGachaPoolItem;
import com.ruoyi.project.domain.entity.BizGachaRecord;
import com.ruoyi.project.domain.entity.BizItem;
import com.ruoyi.project.domain.entity.BizUserAsset;
import com.ruoyi.project.mapper.BizGachaPoolItemMapper;
import com.ruoyi.project.service.IBizGachaPoolService;
import com.ruoyi.project.service.IBizGachaRecordService;
import com.ruoyi.project.service.IBizItemService;
import com.ruoyi.project.service.IBizUserAssetService;
import com.ruoyi.project.service.IBizUserFragmentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RestController
@RequestMapping("/api/gacha")
@RequiredArgsConstructor
@Validated
public class BizGachaController {

    private final IBizGachaPoolService gachaPoolService;
    private final IBizGachaRecordService gachaRecordService;
    private final IBizItemService itemService;
    private final IBizUserAssetService assetService;
    private final IBizUserFragmentService fragmentService;
    private final BizGachaPoolItemMapper poolItemMapper;
    private final LuaScriptExecutor luaScriptExecutor;

    @GetMapping("/page")
    public Result<Page<BizGachaPool>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "8") long pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        Page<BizGachaPool> page = gachaPoolService.pagePools(pageNum, pageSize, name, status);
        return Result.success(page);
    }

    @GetMapping("/active")
    public Result<JSONArray> getActivePools() {
        JSONArray pools = gachaPoolService.getActivePools();
        return Result.success(pools);
    }

    @GetMapping("/{id}")
    public Result<BizGachaPool> getById(@PathVariable Long id) {
        BizGachaPool pool = gachaPoolService.getById(id);
        if (pool == null) {
            return Result.error("奖池不存在");
        }
        return Result.success(pool);
    }

    @PostMapping
    public Result<Boolean> create(@RequestBody BizGachaPool pool) {
        boolean success = gachaPoolService.createPool(pool);
        if (success) {
            return Result.success(true);
        }
        return Result.error("创建失败");
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody BizGachaPool pool) {
        if (pool.getId() == null) {
            return Result.error("奖池ID不能为空");
        }
        boolean success = gachaPoolService.updatePool(pool);
        if (success) {
            return Result.success(true);
        }
        return Result.error("更新失败");
    }

    @PostMapping("/{id}/end")
    public Result<Boolean> endPool(@PathVariable Long id) {
        boolean success = gachaPoolService.endPool(id);
        if (success) {
            return Result.success(true);
        }
        return Result.error("结束奖池失败");
    }

    @GetMapping("/{poolId}/prizes")
    public Result<List<GachaPrizeDTO>> getPrizes(@PathVariable Long poolId) {
        List<BizGachaPoolItem> items = poolItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizGachaPoolItem>()
                        .eq(BizGachaPoolItem::getPoolId, poolId)
                        .eq(BizGachaPoolItem::getDelFlag, 0)
                        .orderByDesc(BizGachaPoolItem::getCreateTime)
        );

        List<GachaPrizeDTO> prizes = new ArrayList<>();
        for (BizGachaPoolItem item : items) {
            BizItem bizItem = itemService.getById(item.getItemId());
            GachaPrizeDTO prize = new GachaPrizeDTO();
            prize.setId(item.getId());
            prize.setPoolId(item.getPoolId());
            prize.setItemName(bizItem != null ? bizItem.getName() : "未知物品");
            prize.setItemImage(bizItem != null ? bizItem.getImage() : "");
            prize.setRarity(item.getRarity());
            prize.setQuantity(item.getStockLimit() != null ? item.getStockLimit() : 9999);
            prize.setWeight(item.getWeight());
            prizes.add(prize);
        }
        return Result.success(prizes);
    }

    @PostMapping("/{poolId}/prizes")
    public Result<Boolean> createPrize(@PathVariable Long poolId, @RequestBody GachaPrizeDTO prize) {
        BizGachaPoolItem item = new BizGachaPoolItem();
        item.setPoolId(poolId);
        item.setRarity(prize.getRarity());
        item.setWeight(prize.getWeight());
        item.setIsGuarantee(0);
        item.setStockLimit(prize.getQuantity());
        item.setDelFlag(0);
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());

        BizItem bizItem = new BizItem();
        String itemKey = "item_" + prize.getRarity().toLowerCase() + "_" + System.currentTimeMillis();
        bizItem.setItemKey(itemKey);
        bizItem.setName(prize.getItemName());
        bizItem.setImage(prize.getItemImage());
        bizItem.setRarity(prize.getRarity());
        bizItem.setType("角色");
        bizItem.setDescription("");
        bizItem.setDelFlag(0);
        bizItem.setCreateTime(LocalDateTime.now());
        bizItem.setUpdateTime(LocalDateTime.now());
        itemService.save(bizItem);

        item.setItemId(bizItem.getId());
        poolItemMapper.insert(item);

        return Result.success(true);
    }

    @PutMapping("/prizes")
    public Result<Boolean> updatePrize(@RequestBody GachaPrizeDTO prize) {
        if (prize.getId() == null) {
            return Result.error("奖品ID不能为空");
        }

        BizGachaPoolItem item = poolItemMapper.selectById(prize.getId());
        if (item == null) {
            return Result.error("奖品不存在");
        }

        item.setRarity(prize.getRarity());
        item.setWeight(prize.getWeight());
        item.setStockLimit(prize.getQuantity());
        item.setUpdateTime(LocalDateTime.now());
        poolItemMapper.updateById(item);

        BizItem bizItem = itemService.getById(item.getItemId());
        if (bizItem != null) {
            bizItem.setName(prize.getItemName());
            bizItem.setImage(prize.getItemImage());
            bizItem.setRarity(prize.getRarity());
            itemService.updateById(bizItem);
        }

        return Result.success(true);
    }

    @DeleteMapping("/prizes/{id}")
    public Result<Boolean> deletePrize(@PathVariable Long id) {
        BizGachaPoolItem item = poolItemMapper.selectById(id);
        if (item == null) {
            return Result.error("奖品不存在");
        }

        item.setDelFlag(1);
        item.setUpdateTime(LocalDateTime.now());
        poolItemMapper.updateById(item);

        return Result.success(true);
    }

    @GetMapping("/records")
    public Result<Page<BizGachaRecord>> getRecords(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize) {
        Long userId = getCurrentUserId();
        Page<BizGachaRecord> page = gachaRecordService.pageRecords(pageNum, pageSize, userId);
        return Result.success(page);
    }

    @PostMapping("/draw")
    @Idempotent(prefix = "gacha_draw", expireTime = 10, message = "抽赏操作过于频繁，请稍后再试")
    @RateLimiterAndCircuitBreaker(rateLimiterName = "gachaDraw", circuitBreakerName = "gachaService")
    public Result<DrawResult> draw(@RequestBody DrawRequest request) {
        Long userId = getCurrentUserId();
        Long poolId = request.getPoolId();
        int count = request.getCount();

        if (poolId == null || (count != 1 && count != 10)) {
            return Result.error("参数错误");
        }

        BizGachaPool pool = gachaPoolService.getById(poolId);
        if (pool == null || pool.getDelFlag() != 0) {
            return Result.error("奖池不存在");
        }

        if (!gachaPoolService.isPoolAvailable(poolId)) {
            return Result.error("奖池不可用或已结束");
        }

        int cost = count == 10 ? pool.getTenCost() : pool.getSingleCost();

        // 使用Lua脚本原子化扣减库存和积分
        boolean deductSuccess = luaScriptExecutor.executeGachaDeduct(
                poolId, userId, count, pool.getSingleCost(), pool.getTenCost());
        
        if (!deductSuccess) {
            int currentStock = luaScriptExecutor.getPoolStock(poolId);
            int userPoints = luaScriptExecutor.getUserPoints(userId);
            
            if (currentStock < count) {
                return Result.error("库存不足，剩余库存: " + currentStock + "，需要: " + count);
            }
            if (userPoints < cost) {
                return Result.error("积分不足");
            }
            return Result.error("扣减失败");
        }

        Long userDrawCount = getUserDrawCount(userId, poolId);
        boolean guaranteed = false;
        String guaranteedRarity = null;

        List<DrawItem> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            userDrawCount++;
            String rarity = rollRarity(pool, userDrawCount, pool.getGuaranteeCount(), pool.getGuaranteeType());

            if ("SSR_GUARANTEED".equals(rarity)) {
                rarity = "SSR";
                guaranteed = true;
                guaranteedRarity = "SSR";
            } else if ("SR_GUARANTEED".equals(rarity)) {
                rarity = "SR";
                guaranteed = true;
                guaranteedRarity = "SR";
            }

            BizItem item = rollItem(poolId, rarity);

            if (item != null) {
                DrawItem drawItem = new DrawItem();
                drawItem.setId(item.getId());
                drawItem.setName(item.getName());
                drawItem.setImage(item.getImage());
                drawItem.setRarity(rarity);
                drawItem.setType(item.getType());
                items.add(drawItem);

                createUserAsset(userId, item, poolId);
            }
        }

        if (count == 10) {
            boolean hasSRPlus = items.stream()
                    .anyMatch(item -> "SSR".equals(item.getRarity()) || "SR".equals(item.getRarity()));
            if (!hasSRPlus) {
                guaranteed = true;
                String targetRarity = "SR";
                BizItem item = rollItem(poolId, targetRarity);
                if (item != null) {
                    DrawItem drawItem = new DrawItem();
                    drawItem.setId(item.getId());
                    drawItem.setName(item.getName());
                    drawItem.setImage(item.getImage());
                    drawItem.setRarity(targetRarity);
                    drawItem.setType(item.getType());
                    if (!items.isEmpty()) {
                        items.set(items.size() - 1, drawItem);
                    } else {
                        items.add(drawItem);
                    }
                }
                if (guaranteedRarity == null) {
                    guaranteedRarity = targetRarity;
                }
            }
        }

        if (userDrawCount >= 70) {
            boolean hasSSR = items.stream().anyMatch(item -> "SSR".equals(item.getRarity()));
            if (!hasSSR) {
                guaranteed = true;
                guaranteedRarity = "SSR";
                BizItem ssrItem = rollItem(poolId, "SSR");
                if (ssrItem != null) {
                    DrawItem drawItem = new DrawItem();
                    drawItem.setId(ssrItem.getId());
                    drawItem.setName(ssrItem.getName());
                    drawItem.setImage(ssrItem.getImage());
                    drawItem.setRarity("SSR");
                    drawItem.setType(ssrItem.getType());
                    if (!items.isEmpty()) {
                        items.set(items.size() - 1, drawItem);
                    } else {
                        items.add(drawItem);
                    }
                }
            }
        }

        // 更新MySQL库存（异步，最终一致性）
        gachaPoolService.decrementStock(poolId, count);

        BizGachaRecord record = new BizGachaRecord();
        record.setUserId(userId);
        record.setPoolId(poolId);
        record.setPoolName(pool.getName());
        record.setGachaType(count);
        record.setCostPoints(cost);
        record.setResultItems(JSON.toJSONString(items));
        record.setIsGuaranteed(guaranteed ? 1 : 0);
        record.setStatus(1);
        gachaRecordService.saveRecord(record);

        int fragmentCount = ThreadLocalRandom.current().nextInt(1, 100);
        fragmentService.addFragment(userId, fragmentCount, "GACHA", record.getId().toString());

        DrawResult result = new DrawResult();
        result.setRecords(items);
        result.setIsGuaranteed(guaranteed);
        result.setGuaranteedRarity(guaranteedRarity);
        result.setFragmentCount(fragmentCount);
        return Result.success(result);
    }

    private Long getUserDrawCount(Long userId, Long poolId) {
        List<BizGachaRecord> records = gachaRecordService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizGachaRecord>()
                        .eq(BizGachaRecord::getUserId, userId)
                        .eq(BizGachaRecord::getPoolId, poolId)
                        .eq(BizGachaRecord::getStatus, 1)
        );
        if (records == null) {
            return 0L;
        }
        return records.stream()
                .mapToLong(r -> r.getGachaType() != null ? r.getGachaType() : 0)
                .sum();
    }

    private void createUserAsset(Long userId, BizItem item, Long poolId) {
        BizUserAsset asset = new BizUserAsset();
        asset.setUserId(userId);
        asset.setItemId(item.getId());
        asset.setAssetKey(userId + "_" + item.getId() + "_" + System.currentTimeMillis());
        asset.setQuantity(1);
        asset.setStatus(1);
        asset.setIsPhysical(0);
        asset.setAcquireType("gacha");
        asset.setAcquireSourceId(poolId.toString());
        asset.setItemName(item.getName());
        asset.setItemImage(item.getImage());
        asset.setItemRarity(item.getRarity());
        asset.setItemType(item.getType());
        asset.setCreateTime(LocalDateTime.now());
        asset.setUpdateTime(LocalDateTime.now());
        asset.setDelFlag(0);
        assetService.save(asset);
    }

    private String rollRarity(BizGachaPool pool, Long userDrawCount, Integer guaranteeCount, String guaranteeType) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int roll = random.nextInt(1000);

        if (guaranteeCount != null && guaranteeCount > 0) {
            if (userDrawCount % guaranteeCount == 0) {
                if ("count".equals(guaranteeType)) {
                    return "SSR_GUARANTEED";
                }
            }

            if (userDrawCount % guaranteeCount == guaranteeCount - 1) {
                if ("rarity".equals(guaranteeType)) {
                    return "SR_GUARANTEED";
                }
            }
        }

        if (roll < 3) {
            return "SSR";
        } else if (roll < 23) {
            return "SR";
        } else if (roll < 223) {
            return "R";
        } else {
            return "N";
        }
    }

    private BizItem rollItem(Long poolId, String rarity) {
        List<BizItem> items = itemService.listByRarity(rarity);
        if (items == null || items.isEmpty()) {
            items = itemService.list();
        }
        if (items == null || items.isEmpty()) {
            BizItem defaultItem = new BizItem();
            defaultItem.setId(1L);
            defaultItem.setName("默认物品");
            defaultItem.setImage("https://picsum.photos/seed/default/200/200");
            defaultItem.setRarity(rarity);
            defaultItem.setType("角色");
            return defaultItem;
        }
        return items.get(ThreadLocalRandom.current().nextInt(items.size()));
    }

    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            return 1L;
        }
    }

    @Data
    public static class DrawRequest {
        private Long poolId;
        private Integer count;
    }

    @Data
    public static class DrawItem {
        private Long id;
        private String name;
        private String image;
        private String rarity;
        private String type;
    }

    @Data
    public static class DrawResult {
        private List<DrawItem> records;
        private Boolean isGuaranteed;
        private String guaranteedRarity;
        private Integer fragmentCount;
    }
}
