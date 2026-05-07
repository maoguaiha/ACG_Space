package com.ruoyi.project.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.project.common.annotation.Idempotent;
import com.ruoyi.project.common.annotation.RateLimiterAndCircuitBreaker;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.entity.BizItem;
import com.ruoyi.project.domain.entity.BizSynthesizeRecipe;
import com.ruoyi.project.domain.entity.BizUserAsset;
import com.ruoyi.project.service.IBizItemService;
import com.ruoyi.project.service.IBizSynthesizeService;
import com.ruoyi.project.service.IBizUserAssetService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/synthesize")
@RequiredArgsConstructor
@Validated
public class BizSynthesizeController {

    private final IBizSynthesizeService synthesizeService;
    private final IBizItemService itemService;
    private final IBizUserAssetService assetService;

    @GetMapping("/recipes")
    public Result<List<SynthesizeRecipeVO>> listRecipes() {
        Long userId = getCurrentUserId();
        List<BizSynthesizeRecipe> recipes = synthesizeService.list(
                new LambdaQueryWrapper<BizSynthesizeRecipe>()
                        .eq(BizSynthesizeRecipe::getDelFlag, 0)
                        .eq(BizSynthesizeRecipe::getStatus, 1)
                        .orderByDesc(BizSynthesizeRecipe::getCreateTime)
        );

        List<SynthesizeRecipeVO> result = new ArrayList<>();
        for (BizSynthesizeRecipe recipe : recipes) {
            SynthesizeRecipeVO vo = new SynthesizeRecipeVO();
            vo.setId(recipe.getId());
            vo.setName(recipe.getName());
            vo.setSuccessRate(recipe.getSuccessRate());
            vo.setCostPoints(recipe.getCostPoints());

            BizItem resultItem = itemService.getById(recipe.getResultItemId());
            if (resultItem != null) {
                vo.setResultItemName(resultItem.getName());
                vo.setResultItemImage(resultItem.getImage());
                vo.setResultItemRarity(resultItem.getRarity());
            }

            JSONArray costItems = JSON.parseArray(recipe.getCostItems());
            List<CostItemDTO> costList = new ArrayList<>();
            for (int i = 0; i < costItems.size(); i++) {
                JSONObject costItem = costItems.getJSONObject(i);
                Long itemId = costItem.getLong("itemId");
                Integer count = costItem.getInteger("count");

                BizItem item = itemService.getById(itemId);
                CostItemDTO dto = new CostItemDTO();
                dto.setItemId(itemId);
                dto.setItemName(item != null ? item.getName() : "未知");
                dto.setItemImage(item != null ? item.getImage() : "");
                dto.setNeedCount(count);

                int ownedCount = getOwnedCount(userId, itemId);
                dto.setOwnedCount(ownedCount);
                dto.setEnough(ownedCount >= count);

                costList.add(dto);
            }
            vo.setCostItems(costList);

            boolean canSynthesize = costList.stream().allMatch(CostItemDTO::getEnough);
            vo.setCanSynthesize(canSynthesize);

            result.add(vo);
        }

        return Result.success(result);
    }

    private int getOwnedCount(Long userId, Long itemId) {
        List<BizUserAsset> assets = assetService.list(
                new LambdaQueryWrapper<BizUserAsset>()
                        .eq(BizUserAsset::getUserId, userId)
                        .eq(BizUserAsset::getItemId, itemId)
                        .eq(BizUserAsset::getStatus, 1)
                        .eq(BizUserAsset::getDelFlag, 0)
        );
        return assets.stream().mapToInt(BizUserAsset::getQuantity).sum();
    }

    @PostMapping("/do")
    @Idempotent(prefix = "synthesize_do", expireTime = 15, message = "合成操作过于频繁，请稍后再试")
    @RateLimiterAndCircuitBreaker(rateLimiterName = "synthesizeDo", circuitBreakerName = "synthesizeService")
    public Result<SynthesizeResultDTO> synthesize(@RequestBody @Validated SynthesizeRequest request) {
        Long userId = getCurrentUserId();
        IBizSynthesizeService.SynthesizeResultDTO result = synthesizeService.synthesize(userId, request.getRecipeId());
        
        SynthesizeResultDTO dto = new SynthesizeResultDTO();
        dto.setSuccess(result.getSuccess());
        dto.setMessage(result.getMessage());
        dto.setAssetId(result.getAssetId());
        dto.setItemName(result.getItemName());
        dto.setItemImage(result.getItemImage());
        dto.setItemRarity(result.getItemRarity());
        
        if (result.getSuccess()) {
            return Result.success(dto);
        }
        return Result.error(dto.getMessage());
    }

    private Long getCurrentUserId() {
        try {
            Long userId = SecurityUtils.getUserId();
            return userId != null ? userId : 1L;
        } catch (Exception e) {
            return 1L;
        }
    }

    @Data
    public static class SynthesizeRequest {
        private Long recipeId;
    }

    @Data
    public static class SynthesizeResultDTO {
        private Boolean success;
        private String message;
        private Long assetId;
        private String itemName;
        private String itemImage;
        private String itemRarity;
    }

    @Data
    public static class SynthesizeRecipeVO {
        private Long id;
        private String name;
        private Integer successRate;
        private Integer costPoints;
        private String resultItemName;
        private String resultItemImage;
        private String resultItemRarity;
        private List<CostItemDTO> costItems;
        private Boolean canSynthesize;
    }

    @Data
    public static class CostItemDTO {
        private Long itemId;
        private String itemName;
        private String itemImage;
        private Integer needCount;
        private Integer ownedCount;
        private Boolean enough;
    }
}