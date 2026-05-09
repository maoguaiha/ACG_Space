package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.domain.entity.BizItem;
import com.ruoyi.project.domain.entity.BizSynthesizeRule;
import com.ruoyi.project.domain.entity.BizUserAsset;
import com.ruoyi.project.mapper.BizSynthesizeRuleMapper;
import com.ruoyi.project.mapper.BizUserAssetMapper;
import com.ruoyi.project.service.IBizItemService;
import com.ruoyi.project.service.IBizSynthesizeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizSynthesizeServiceImpl extends ServiceImpl<BizSynthesizeRuleMapper, BizSynthesizeRule> implements IBizSynthesizeService {

    private final BizUserAssetMapper userAssetMapper;
    private final IBizItemService itemService;

    @Override
    public List<BizSynthesizeRule> getActiveRules() {
        return list(new LambdaQueryWrapper<BizSynthesizeRule>()
                .eq(BizSynthesizeRule::getStatus, 1)
                .eq(BizSynthesizeRule::getDelFlag, 0)
                .orderByAsc(BizSynthesizeRule::getSourceRarity));
    }

    @Override
    public int getUserAssetCountByRarity(Long userId, String rarity) {
        List<BizUserAsset> assets = userAssetMapper.selectList(new LambdaQueryWrapper<BizUserAsset>()
                .eq(BizUserAsset::getUserId, userId)
                .eq(BizUserAsset::getItemRarity, rarity)
                .eq(BizUserAsset::getStatus, 1)
                .eq(BizUserAsset::getDelFlag, 0));
        if (assets == null || assets.isEmpty()) {
            return 0;
        }
        return assets.stream().mapToInt(a -> a.getQuantity() != null ? a.getQuantity() : 1).sum();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean synthesize(Long userId, String sourceRarity, int times) {
        BizSynthesizeRule rule = getOne(new LambdaQueryWrapper<BizSynthesizeRule>()
                .eq(BizSynthesizeRule::getSourceRarity, sourceRarity)
                .eq(BizSynthesizeRule::getStatus, 1)
                .eq(BizSynthesizeRule::getDelFlag, 0));

        if (rule == null) {
            throw new RuntimeException("合成规则不存在");
        }

        int requiredCount = rule.getSourceCount() * times;
        int userCount = getUserAssetCountByRarity(userId, sourceRarity);

        if (userCount < requiredCount) {
            throw new RuntimeException("材料不足，需要 " + requiredCount + " 个 " + sourceRarity + " 品质物品");
        }

        List<BizUserAsset> assets = userAssetMapper.selectList(new LambdaQueryWrapper<BizUserAsset>()
                .eq(BizUserAsset::getUserId, userId)
                .eq(BizUserAsset::getItemRarity, sourceRarity)
                .eq(BizUserAsset::getStatus, 1)
                .eq(BizUserAsset::getDelFlag, 0)
                .orderByAsc(BizUserAsset::getCreateTime));

        int remaining = requiredCount;
        for (BizUserAsset asset : assets) {
            if (remaining <= 0) {
                break;
            }
            int qty = asset.getQuantity() != null ? asset.getQuantity() : 1;
            if (qty <= remaining) {
                asset.setStatus(4);
                asset.setUpdateTime(LocalDateTime.now());
                userAssetMapper.updateById(asset);
                remaining -= qty;
            } else {
                asset.setQuantity(qty - remaining);
                asset.setUpdateTime(LocalDateTime.now());
                userAssetMapper.updateById(asset);
                remaining = 0;
            }
        }

        boolean isPhysical = rule.getIsPhysical() != null && rule.getIsPhysical() == 1;
        String targetRarity = rule.getTargetRarity();
        createFragmentAsset(userId, targetRarity, times, isPhysical);

        log.info("用户 {} 合成成功: {} 个 {} -> {} 个 {}碎片", userId, requiredCount, sourceRarity, times, targetRarity);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean synthesizeByIds(Long userId, String sourceRarity, List<Long> assetIds) {
        if (assetIds == null || assetIds.size() != 10) {
            throw new RuntimeException("请选择10个物品进行合成");
        }

        BizSynthesizeRule rule = getOne(new LambdaQueryWrapper<BizSynthesizeRule>()
                .eq(BizSynthesizeRule::getSourceRarity, sourceRarity)
                .eq(BizSynthesizeRule::getStatus, 1)
                .eq(BizSynthesizeRule::getDelFlag, 0));

        if (rule == null) {
            throw new RuntimeException("合成规则不存在");
        }

        List<BizUserAsset> assets = userAssetMapper.selectList(new LambdaQueryWrapper<BizUserAsset>()
                .eq(BizUserAsset::getUserId, userId)
                .in(BizUserAsset::getId, assetIds)
                .eq(BizUserAsset::getItemRarity, sourceRarity)
                .eq(BizUserAsset::getStatus, 1)
                .eq(BizUserAsset::getDelFlag, 0));

        log.info("合成校验: userId={}, sourceRarity={}, assetIds={}, 查询到资产数={}", userId, sourceRarity, assetIds, assets.size());
        if (assets.size() < 10) {
            List<BizUserAsset> allAssets = userAssetMapper.selectList(new LambdaQueryWrapper<BizUserAsset>()
                    .eq(BizUserAsset::getUserId, userId)
                    .in(BizUserAsset::getId, assetIds));
            log.info("全部资产(不限状态): {}", allAssets.stream().map(a -> a.getId() + "(rarity=" + a.getItemRarity() + ",status=" + a.getStatus() + ")").collect(java.util.stream.Collectors.joining(", ")));
        }

        if (assets.size() != 10) {
            throw new RuntimeException("所选物品不合法，请重新选择");
        }

        for (BizUserAsset asset : assets) {
            asset.setStatus(4);
            asset.setUpdateTime(LocalDateTime.now());
            userAssetMapper.updateById(asset);
        }

        boolean isPhysical = rule.getIsPhysical() != null && rule.getIsPhysical() == 1;
        String targetRarity = rule.getTargetRarity();
        createFragmentAsset(userId, targetRarity, 1, isPhysical);

        log.info("用户 {} 合成成功: 10个 {} -> 1个 {}碎片", userId, sourceRarity, targetRarity);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean synthesizeByItems(Long userId, String sourceRarity, List<?> selectedItems, int times) {
        if (selectedItems == null || selectedItems.isEmpty()) {
            throw new RuntimeException("请选择合成材料");
        }

        int totalCount = 0;
        for (Object item : selectedItems) {
            try {
                java.lang.reflect.Method getCountMethod = item.getClass().getMethod("getCount");
                Integer count = (Integer) getCountMethod.invoke(item);
                totalCount += count;
            } catch (Exception e) {
                throw new RuntimeException("合成数据格式错误");
            }
        }

        int requiredCount = 10 * times;
        if (totalCount != requiredCount) {
            throw new RuntimeException("请选择" + requiredCount + "个物品进行合成，当前已选 " + totalCount + " 个");
        }

        BizSynthesizeRule rule = getOne(new LambdaQueryWrapper<BizSynthesizeRule>()
                .eq(BizSynthesizeRule::getSourceRarity, sourceRarity)
                .eq(BizSynthesizeRule::getStatus, 1)
                .eq(BizSynthesizeRule::getDelFlag, 0));

        if (rule == null) {
            throw new RuntimeException("合成规则不存在");
        }

        List<Long> assetIds = new java.util.ArrayList<>();
        for (Object item : selectedItems) {
            try {
                java.lang.reflect.Method getAssetIdMethod = item.getClass().getMethod("getAssetId");
                Long assetId = (Long) getAssetIdMethod.invoke(item);
                assetIds.add(assetId);
            } catch (Exception e) {
                throw new RuntimeException("合成数据格式错误");
            }
        }

        List<BizUserAsset> assets = userAssetMapper.selectList(new LambdaQueryWrapper<BizUserAsset>()
                .eq(BizUserAsset::getUserId, userId)
                .in(BizUserAsset::getId, assetIds)
                .eq(BizUserAsset::getItemRarity, sourceRarity)
                .eq(BizUserAsset::getStatus, 1)
                .eq(BizUserAsset::getDelFlag, 0));

        log.info("合成校验(按数量): userId={}, sourceRarity={}, times={}, 查询到资产数={}", userId, sourceRarity, times, assets.size());

        if (assets.size() != selectedItems.size()) {
            throw new RuntimeException("所选物品不合法，请重新选择");
        }

        for (Object item : selectedItems) {
            Long assetId;
            int useCount;
            try {
                java.lang.reflect.Method getAssetIdMethod = item.getClass().getMethod("getAssetId");
                java.lang.reflect.Method getCountMethod = item.getClass().getMethod("getCount");
                assetId = (Long) getAssetIdMethod.invoke(item);
                useCount = (Integer) getCountMethod.invoke(item);
            } catch (Exception e) {
                throw new RuntimeException("合成数据格式错误");
            }

            BizUserAsset asset = assets.stream()
                    .filter(a -> a.getId().equals(assetId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("物品不存在"));

            int qty = asset.getQuantity() != null ? asset.getQuantity() : 1;

            if (useCount > qty) {
                throw new RuntimeException("物品 " + asset.getItemName() + " 数量不足，需要 " + useCount + " 个，仅有 " + qty + " 个");
            }

            if (useCount >= qty) {
                asset.setStatus(4);
                asset.setUpdateTime(LocalDateTime.now());
                userAssetMapper.updateById(asset);
            } else {
                asset.setQuantity(qty - useCount);
                asset.setUpdateTime(LocalDateTime.now());
                userAssetMapper.updateById(asset);
            }
        }

        boolean isPhysical = rule.getIsPhysical() != null && rule.getIsPhysical() == 1;
        String targetRarity = rule.getTargetRarity();
        createFragmentAsset(userId, targetRarity, times, isPhysical);

        log.info("用户 {} 合成成功(按数量): {} 个 {} -> {}个 {}碎片", userId, totalCount, sourceRarity, times, targetRarity);
        return true;
    }

    @Override
    public BizUserAsset createSynthesizedAsset(Long userId, String targetRarity, boolean isPhysical) {
        List<BizItem> items = itemService.listByRarity(targetRarity);
        BizItem item;
        if (items == null || items.isEmpty()) {
            item = new BizItem();
            item.setId(System.currentTimeMillis());
            item.setName("合成" + targetRarity + "物品");
            item.setImage("https://picsum.photos/seed/synth_" + targetRarity + "/200/200");
            item.setRarity(targetRarity);
            item.setType("角色");
            item.setDelFlag(0);
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            itemService.save(item);
        } else {
            item = items.get(0);
        }

        BizUserAsset asset = new BizUserAsset();
        asset.setUserId(userId);
        asset.setItemId(item.getId());
        asset.setAssetKey(userId + "_" + item.getId() + "_" + System.currentTimeMillis());
        asset.setQuantity(1);
        asset.setStatus(1);
        asset.setIsPhysical(isPhysical ? 1 : 0);
        asset.setAcquireType("synthesize");
        asset.setAcquireSourceId("synthesize_" + System.currentTimeMillis());
        asset.setItemName(item.getName());
        asset.setItemImage(item.getImage());
        asset.setItemRarity(targetRarity);
        asset.setItemType(item.getType());
        asset.setCreateTime(LocalDateTime.now());
        asset.setUpdateTime(LocalDateTime.now());
        asset.setDelFlag(0);
        userAssetMapper.insert(asset);

        return asset;
    }

    private void createFragmentAsset(Long userId, String targetRarity, int count, boolean isPhysical) {
        String fragmentName = targetRarity + "碎片";
        String fragmentImage = getFragmentImage(targetRarity);
        String assetKey = userId + "_fragment_" + targetRarity;

        BizUserAsset existingAsset = userAssetMapper.selectOne(new LambdaQueryWrapper<BizUserAsset>()
                .eq(BizUserAsset::getUserId, userId)
                .eq(BizUserAsset::getAssetKey, assetKey)
                .eq(BizUserAsset::getStatus, 1)
                .eq(BizUserAsset::getDelFlag, 0));

        if (existingAsset != null) {
            existingAsset.setQuantity(existingAsset.getQuantity() + count);
            existingAsset.setUpdateTime(LocalDateTime.now());
            userAssetMapper.updateById(existingAsset);
        } else {
            BizUserAsset asset = new BizUserAsset();
            asset.setUserId(userId);
            asset.setItemId(0L);
            asset.setAssetKey(assetKey);
            asset.setQuantity(count);
            asset.setStatus(1);
            asset.setIsPhysical(isPhysical ? 1 : 0);
            asset.setAcquireType("synthesize");
            asset.setAcquireSourceId("synthesize_" + System.currentTimeMillis());
            asset.setItemName(fragmentName);
            asset.setItemImage(fragmentImage);
            asset.setItemRarity(targetRarity);
            asset.setItemType("fragment");
            asset.setCreateTime(LocalDateTime.now());
            asset.setUpdateTime(LocalDateTime.now());
            asset.setDelFlag(0);
            userAssetMapper.insert(asset);
        }
    }

    private String getFragmentImage(String rarity) {
        switch (rarity) {
            case "SR":
                return "data:image/svg+xml," + java.net.URLEncoder.encode(
                        "<svg xmlns='http://www.w3.org/2000/svg' width='200' height='200'>" +
                        "<defs><linearGradient id='g' x1='0%' y1='0%' x2='100%' y2='100%'>" +
                        "<stop offset='0%' style='stop-color:#a855f7'/><stop offset='100%' style='stop-color:#ec4899'/>" +
                        "</linearGradient></defs>" +
                        "<rect width='200' height='200' rx='20' fill='url(%23g)'/>" +
                        "<text x='100' y='110' text-anchor='middle' fill='white' font-size='60' font-weight='bold'>SR</text>" +
                        "</svg>", java.nio.charset.StandardCharsets.UTF_8);
            case "SSR":
                return "data:image/svg+xml," + java.net.URLEncoder.encode(
                        "<svg xmlns='http://www.w3.org/2000/svg' width='200' height='200'>" +
                        "<defs><linearGradient id='g' x1='0%' y1='0%' x2='100%' y2='100%'>" +
                        "<stop offset='0%' style='stop-color:#f59e0b'/><stop offset='100%' style='stop-color:#f97316'/>" +
                        "</linearGradient></defs>" +
                        "<rect width='200' height='200' rx='20' fill='url(%23g)'/>" +
                        "<text x='100' y='110' text-anchor='middle' fill='white' font-size='60' font-weight='bold'>SSR</text>" +
                        "</svg>", java.nio.charset.StandardCharsets.UTF_8);
            case "UR":
                return "data:image/svg+xml," + java.net.URLEncoder.encode(
                        "<svg xmlns='http://www.w3.org/2000/svg' width='200' height='200'>" +
                        "<defs><linearGradient id='g' x1='0%' y1='0%' x2='100%' y2='100%'>" +
                        "<stop offset='0%' style='stop-color:#ef4444'/><stop offset='100%' style='stop-color:#f43f5e'/>" +
                        "</linearGradient></defs>" +
                        "<rect width='200' height='200' rx='20' fill='url(%23g)'/>" +
                        "<text x='100' y='110' text-anchor='middle' fill='white' font-size='60' font-weight='bold'>UR</text>" +
                        "</svg>", java.nio.charset.StandardCharsets.UTF_8);
            default:
                return "https://picsum.photos/seed/fragment_" + rarity + "/200/200";
        }
    }
}
