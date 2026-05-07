package com.ruoyi.project.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.domain.entity.BizItem;
import com.ruoyi.project.domain.entity.BizSynthesizeRecipe;
import com.ruoyi.project.domain.entity.BizSynthesizeRecord;
import com.ruoyi.project.domain.entity.BizUserAsset;
import com.ruoyi.project.mapper.BizSynthesizeRecipeMapper;
import com.ruoyi.project.mapper.BizSynthesizeRecordMapper;
import com.ruoyi.project.service.IBizItemService;
import com.ruoyi.project.service.IBizSynthesizeService;
import com.ruoyi.project.service.IBizUserAssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizSynthesizeServiceImpl extends ServiceImpl<BizSynthesizeRecipeMapper, BizSynthesizeRecipe>
        implements IBizSynthesizeService {

    private final IBizItemService itemService;
    private final IBizUserAssetService assetService;
    private final BizSynthesizeRecordMapper recordMapper;
    private final RedissonClient redissonClient;

    private static final String SYNTHESIZE_LOCK_PREFIX = "synthesize:lock:";

    @Override
    public Page<BizSynthesizeRecipe> pageRecipes(long pageNum, long pageSize, String name) {
        Page<BizSynthesizeRecipe> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizSynthesizeRecipe> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizSynthesizeRecipe::getDelFlag, 0);
        wrapper.eq(BizSynthesizeRecipe::getStatus, 1);

        if (name != null && !name.isBlank()) {
            wrapper.like(BizSynthesizeRecipe::getName, name);
        }

        wrapper.orderByDesc(BizSynthesizeRecipe::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SynthesizeResultDTO synthesize(Long userId, Long recipeId) {
        SynthesizeResultDTO result = new SynthesizeResultDTO();

        // 首先获取配方信息，以便构建锁的key列表
        BizSynthesizeRecipe recipe = this.getById(recipeId);
        if (recipe == null || recipe.getDelFlag() != 0 || recipe.getStatus() != 1) {
            result.setSuccess(false);
            result.setMessage("配方不存在或已禁用");
            return result;
        }

        BizItem resultItem = itemService.getById(recipe.getResultItemId());
        if (resultItem == null) {
            result.setSuccess(false);
            result.setMessage("产物物品不存在");
            return result;
        }

        // 构建锁列表 - 同时锁定所有需要的碎片资源
        JSONArray costItems = JSON.parseArray(recipe.getCostItems());
        java.util.List<RLock> locks = new java.util.ArrayList<>();
        java.util.List<RLock> acquiredLocks = new java.util.ArrayList<>();
        
        // 添加每个材料的锁（防止碎片被重复使用）
        for (int i = 0; i < costItems.size(); i++) {
            JSONObject costItem = costItems.getJSONObject(i);
            Long itemId = costItem.getLong("itemId");
            // 锁的格式: synthesize:lock:asset:{userId}:{itemId}
            locks.add(redissonClient.getLock(SYNTHESIZE_LOCK_PREFIX + "asset:" + userId + ":" + itemId));
        }
        
        // 添加配方级别的锁（防止同一用户重复合成同一配方）
        locks.add(redissonClient.getLock(SYNTHESIZE_LOCK_PREFIX + "recipe:" + recipeId));
        
        // 按锁名称排序，确保所有线程按相同顺序获取锁，避免死锁
        locks.sort(java.util.Comparator.comparing(RLock::getName));

        try {
            // 按排序后的顺序获取所有锁
            for (RLock lock : locks) {
                if (!lock.tryLock(10, 60, TimeUnit.SECONDS)) {
                    // 获取锁失败，释放已获取的锁
                    releaseLocks(acquiredLocks);
                    result.setSuccess(false);
                    result.setMessage("系统繁忙，请稍后再试");
                    return result;
                }
                acquiredLocks.add(lock);
            }

            try {
                // 再次校验材料是否足够（防止锁等待期间被其他操作消耗）
                for (int i = 0; i < costItems.size(); i++) {
                    JSONObject costItem = costItems.getJSONObject(i);
                    Long itemId = costItem.getLong("itemId");
                    Integer needCount = costItem.getInteger("count");

                    List<BizUserAsset> userAssets = assetService.list(
                            new LambdaQueryWrapper<BizUserAsset>()
                                    .eq(BizUserAsset::getUserId, userId)
                                    .eq(BizUserAsset::getItemId, itemId)
                                    .eq(BizUserAsset::getStatus, 1)
                                    .eq(BizUserAsset::getDelFlag, 0)
                    );

                    int totalQuantity = userAssets.stream()
                            .mapToInt(BizUserAsset::getQuantity)
                            .sum();

                    if (totalQuantity < needCount) {
                        BizItem item = itemService.getById(itemId);
                        String itemName = item != null ? item.getName() : "材料";
                        result.setSuccess(false);
                        result.setMessage("材料不足：需要 " + needCount + " 个" + itemName + "，当前拥有 " + totalQuantity);
                        return result;
                    }

                    int remaining = needCount;
                    for (BizUserAsset asset : userAssets) {
                        if (remaining <= 0) break;

                        int consume = Math.min(remaining, asset.getQuantity());
                        if (consume == asset.getQuantity()) {
                            asset.setDelFlag(2);
                        } else {
                            asset.setQuantity(asset.getQuantity() - consume);
                        }
                        asset.setUpdateTime(LocalDateTime.now());
                        assetService.updateById(asset);
                        remaining -= consume;
                    }
                }

                boolean isSuccess = true;
                if (recipe.getSuccessRate() < 100) {
                    int roll = ThreadLocalRandom.current().nextInt(100);
                    isSuccess = roll < recipe.getSuccessRate();
                }

                BizSynthesizeRecord record = new BizSynthesizeRecord();
                record.setUserId(userId);
                record.setRecipeId(recipeId);
                record.setRecipeName(recipe.getName());
                record.setResultItemId(resultItem.getId());
                record.setResultItemName(resultItem.getName());
                record.setResultQuantity(recipe.getResultQuantity());
                record.setCostPoints(recipe.getCostPoints() != null ? recipe.getCostPoints() : 0);
                record.setSuccess(isSuccess);
                record.setStatus(isSuccess ? 2 : 3);
                record.setCreateTime(LocalDateTime.now());
                record.setUpdateTime(LocalDateTime.now());
                record.setDelFlag(0);
                recordMapper.insert(record);

                if (isSuccess) {
                    BizUserAsset newAsset = new BizUserAsset();
                    newAsset.setUserId(userId);
                    newAsset.setItemId(resultItem.getId());
                    newAsset.setAssetKey(userId + "_" + resultItem.getId() + "_" + System.currentTimeMillis());
                    newAsset.setQuantity(recipe.getResultQuantity());
                    newAsset.setStatus(1);
                    newAsset.setAcquireType("synthesize");
                    newAsset.setAcquireSourceId(recipeId.toString());
                    newAsset.setItemName(resultItem.getName());
                    newAsset.setItemImage(resultItem.getImage());
                    newAsset.setItemRarity(resultItem.getRarity());
                    newAsset.setItemType(resultItem.getType());
                    newAsset.setCreateTime(LocalDateTime.now());
                    newAsset.setUpdateTime(LocalDateTime.now());
                    newAsset.setDelFlag(0);
                    assetService.save(newAsset);

                    result.setSuccess(true);
                    result.setMessage("合成成功！");
                    result.setAssetId(newAsset.getId());
                    result.setItemName(resultItem.getName());
                    result.setItemImage(resultItem.getImage());
                    result.setItemRarity(resultItem.getRarity());
                } else {
                    result.setSuccess(false);
                    result.setMessage("合成失败，材料已消耗");
                }

                return result;
            } finally {
                releaseLocks(acquiredLocks);
            }
        } catch (InterruptedException e) {
            log.error("合成获取锁被中断, userId: {}, recipeId: {}", userId, recipeId, e);
            Thread.currentThread().interrupt();
            releaseLocks(acquiredLocks);
            result.setSuccess(false);
            result.setMessage("系统繁忙，请稍后再试");
            return result;
        }
    }

    /**
     * 释放所有已获取的锁
     */
    private void releaseLocks(java.util.List<RLock> locks) {
        for (RLock lock : locks) {
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception e) {
                log.error("释放锁失败", e);
            }
        }
    }

    @Override
    public Page<BizSynthesizeRecord> pageRecords(long pageNum, long pageSize, Long userId) {
        Page<BizSynthesizeRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizSynthesizeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizSynthesizeRecord::getDelFlag, 0);

        if (userId != null) {
            wrapper.eq(BizSynthesizeRecord::getUserId, userId);
        }

        wrapper.orderByDesc(BizSynthesizeRecord::getCreateTime);
        return recordMapper.selectPage(page, wrapper);
    }
}