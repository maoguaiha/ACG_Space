package com.ruoyi.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.entity.BizUserPointsLog;
import com.ruoyi.project.service.impl.BizUserPointsLogServiceImpl;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 积分管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
@Validated
public class BizPointsController {

    private final BizUserPointsLogServiceImpl pointsLogService;

    /**
     * 获取用户积分
     */
    @GetMapping("/balance")
    public Result<PointsBalanceDTO> getBalance() {
        Long userId = getCurrentUserId();
        int points = pointsLogService.getUserPoints(userId);
        
        PointsBalanceDTO dto = new PointsBalanceDTO();
        dto.setUserId(userId);
        dto.setPoints(points);
        
        return Result.success(dto);
    }

    /**
     * 获取当前用户积分（简化版，返回纯数字）
     */
    @GetMapping("/my")
    public Result<Integer> getMyPoints() {
        Long userId = getCurrentUserId();
        int points = pointsLogService.getUserPoints(userId);
        return Result.success(points);
    }

    /**
     * 每日签到
     */
    @PostMapping("/sign-in")
    public Result<SignInResultDTO> signIn() {
        Long userId = getCurrentUserId();
        boolean success = pointsLogService.signIn(userId);
        
        SignInResultDTO dto = new SignInResultDTO();
        dto.setSuccess(success);
        dto.setMessage(success ? "签到成功，获得10积分" : "今日已签到");
        
        return Result.success(dto);
    }

    /**
     * 获取积分流水
     */
    @GetMapping("/history")
    public Result<Page<BizUserPointsLog>> getHistory(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize) {
        Long userId = getCurrentUserId();
        Page<BizUserPointsLog> page = pointsLogService.getPointsLog(userId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 评论发奖（内部调用）
     */
    @PostMapping("/reward/comment")
    public Result<Boolean> rewardComment(@RequestBody RewardRequest request) {
        pointsLogService.addPointsForComment(request.getUserId(), request.getBizId());
        return Result.success(true);
    }

    /**
     * 点赞发奖（内部调用）
     */
    @PostMapping("/reward/like")
    public Result<Boolean> rewardLike(@RequestBody RewardRequest request) {
        pointsLogService.addPointsForLike(request.getUserId(), request.getBizId());
        return Result.success(true);
    }

    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            return 1L;
        }
    }

    @Data
    public static class PointsBalanceDTO {
        private Long userId;
        private Integer points;
    }

    @Data
    public static class SignInResultDTO {
        private Boolean success;
        private String message;
    }

    @Data
    public static class RewardRequest {
        private Long userId;
        private Long bizId;
    }
}