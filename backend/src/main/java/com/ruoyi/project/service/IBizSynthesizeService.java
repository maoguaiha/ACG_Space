package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizSynthesizeRule;
import com.ruoyi.project.domain.entity.BizUserAsset;

import java.util.List;

public interface IBizSynthesizeService extends IService<BizSynthesizeRule> {

    List<BizSynthesizeRule> getActiveRules();

    int getUserAssetCountByRarity(Long userId, String rarity);

    boolean synthesize(Long userId, String sourceRarity, int times);

    boolean synthesizeByIds(Long userId, String sourceRarity, List<Long> assetIds);

    boolean synthesizeByItems(Long userId, String sourceRarity, List<?> selectedItems, int times);

    BizUserAsset createSynthesizedAsset(Long userId, String targetRarity, boolean isPhysical);
}
