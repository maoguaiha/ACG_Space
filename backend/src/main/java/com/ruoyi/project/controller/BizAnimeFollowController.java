package com.ruoyi.project.controller;

import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.entity.BizAnime;
import com.ruoyi.project.service.IBizAnimeFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 追番记录控制器
 */
@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class BizAnimeFollowController {

    private final IBizAnimeFollowService followService;

    /**
     * 切换追番状态 (关注/取消关注)
     */
    @PostMapping("/{animeId}")
    public Result<Boolean> toggle(@PathVariable Long animeId) {
        Long userId = SecurityUtils.getUserId();
        boolean followed = followService.toggleFollow(userId, animeId);
        return Result.success(followed);
    }

    /**
     * 获取当前用户的追番列表
     */
    @GetMapping("/list")
    public Result<List<BizAnime>> list() {
        Long userId = SecurityUtils.getUserId();
        return Result.success(followService.getUserFollowList(userId));
    }

    /**
     * 根据 Bangumi ID 关注 (如果不存在则同步)
     */
    @PostMapping("/bangumi/{bgmId}")
    public Result<Boolean> followByBangumi(@PathVariable Integer bgmId) {
        Long userId = SecurityUtils.getUserId();
        boolean followed = followService.followByBangumi(userId, bgmId);
        return Result.success(followed);
    }

    /**
     * 根据 Bangumi ID 检查是否已关注
     */
    @GetMapping("/status/bangumi/{bgmId}")
    public Result<Boolean> statusByBgmId(@PathVariable Integer bgmId) {
        Long userId = SecurityUtils.getUserId();
        return Result.success(followService.isFollowedByBgmId(userId, bgmId));
    }

    /**
     * 检查是否已追番 (本地 ID)
     */
    @GetMapping("/status/{animeId}")
    public Result<Boolean> status(@PathVariable Long animeId) {
        Long userId = SecurityUtils.getUserId();
        return Result.success(followService.isFollowed(userId, animeId));
    }
}
