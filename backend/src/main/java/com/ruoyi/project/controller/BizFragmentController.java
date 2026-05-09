package com.ruoyi.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.entity.BizUserAsset;
import com.ruoyi.project.mapper.BizUserAssetMapper;
import com.ruoyi.project.service.IBizUserFragmentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/fragment")
@RequiredArgsConstructor
@Validated
public class BizFragmentController {

    private final IBizUserFragmentService fragmentService;
    private final BizUserAssetMapper userAssetMapper;

    @GetMapping("/my")
    public Result<Integer> getMyFragment() {
        Long userId = getCurrentUserId();
        log.info("【查询UR碎片】收到请求: userId={}", userId);
        
        // 从 biz_user_asset 表查询 UR 碎片数量
        BizUserAsset urFragmentAsset = userAssetMapper.selectOne(new LambdaQueryWrapper<BizUserAsset>()
                .eq(BizUserAsset::getUserId, userId)
                .eq(BizUserAsset::getItemRarity, "UR")
                .eq(BizUserAsset::getItemType, "fragment")
                .eq(BizUserAsset::getStatus, 1)
                .eq(BizUserAsset::getDelFlag, 0));
        
        int urFragmentCount = urFragmentAsset != null && urFragmentAsset.getQuantity() != null 
                ? urFragmentAsset.getQuantity() : 0;
        
        log.info("【查询UR碎片】查询结果: assetId={}, quantity={}, status={}", 
                urFragmentAsset != null ? urFragmentAsset.getId() : "null",
                urFragmentCount,
                urFragmentAsset != null ? urFragmentAsset.getStatus() : "null");
        return Result.success(urFragmentCount);
    }

    @PostMapping("/exchange")
    public Result<ExchangeResult> exchange(@RequestBody @Validated ExchangeRequest request) {
        Long userId = getCurrentUserId();
        boolean success = fragmentService.exchangeFragmentForPoints(userId, request.getFragmentCount());
        if (success) {
            ExchangeResult result = new ExchangeResult();
            result.setPoints((request.getFragmentCount() / 100) * 10);
            return Result.success(result);
        }
        return Result.error("兑换失败");
    }

    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            return 1L;
        }
    }

    @Data
    public static class ExchangeRequest {
        private Integer fragmentCount;
    }

    @Data
    public static class ExchangeResult {
        private Integer points;
    }
}
