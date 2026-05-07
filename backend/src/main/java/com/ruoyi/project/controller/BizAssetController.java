package com.ruoyi.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.entity.BizUserAsset;
import com.ruoyi.project.service.IBizUserAssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/asset")
@RequiredArgsConstructor
@Validated
public class BizAssetController {

    private final IBizUserAssetService assetService;

    @GetMapping("/page")
    public Result<Page<BizUserAsset>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize) {
        Long userId = getCurrentUserId();
        Page<BizUserAsset> page = assetService.pageUserAssets(pageNum, pageSize, userId);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<BizUserAsset> getById(@PathVariable Long id) {
        BizUserAsset asset = assetService.getById(id);
        if (asset == null) {
            return Result.error("资产不存在");
        }
        return Result.success(asset);
    }

    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            return 1L;
        }
    }
}
