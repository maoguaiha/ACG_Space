package com.ruoyi.project.controller;

import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.entity.BizSynthesizeRule;
import com.ruoyi.project.service.IBizSynthesizeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/synthesize")
@RequiredArgsConstructor
@Validated
public class BizSynthesizeController {

    private final IBizSynthesizeService synthesizeService;

    @GetMapping("/rules")
    public Result<List<BizSynthesizeRule>> getRules() {
        List<BizSynthesizeRule> rules = synthesizeService.getActiveRules();
        return Result.success(rules);
    }

    @GetMapping("/materials")
    public Result<Map<String, Integer>> getMaterials() {
        Long userId = getCurrentUserId();
        Map<String, Integer> materials = new HashMap<>();
        materials.put("R", synthesizeService.getUserAssetCountByRarity(userId, "R"));
        materials.put("SR", synthesizeService.getUserAssetCountByRarity(userId, "SR"));
        materials.put("SSR", synthesizeService.getUserAssetCountByRarity(userId, "SSR"));
        return Result.success(materials);
    }

    @PostMapping("/execute")
    public Result<SynthesizeResult> synthesize(@RequestBody @Validated SynthesizeRequest request) {
        Long userId = getCurrentUserId();
        try {
            int times = request.getTimes() != null && request.getTimes() > 0 ? request.getTimes() : 1;
            boolean success = synthesizeService.synthesizeByItems(userId, request.getSourceRarity(), request.getSelectedItems(), times);
            if (success) {
                SynthesizeResult result = new SynthesizeResult();
                result.setSuccess(true);
                result.setMessage("合成成功");
                return Result.success(result);
            }
            return Result.error("合成失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            return 1L;
        }
    }

    @Data
    public static class SynthesizeRequest {
        private String sourceRarity;
        private List<Long> assetIds;
        private List<SelectedItem> selectedItems;
        private Integer times;
    }

    @Data
    public static class SelectedItem {
        private Long assetId;
        private Integer count;
    }

    @Data
    public static class SynthesizeResult {
        private Boolean success;
        private String message;
    }
}
