package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.domain.entity.BizAnime;
import com.ruoyi.project.domain.entity.BizAnimeFollow;
import com.ruoyi.project.mapper.BizAnimeFollowMapper;
import com.ruoyi.project.service.IBizAnimeFollowService;
import com.ruoyi.project.service.IBizAnimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 追番记录服务实现
 */
@Service
@RequiredArgsConstructor
public class BizAnimeFollowServiceImpl extends ServiceImpl<BizAnimeFollowMapper, BizAnimeFollow> implements IBizAnimeFollowService {

    private final IBizAnimeService animeService;

    @Override
    public boolean toggleFollow(Long userId, Long animeId) {
        BizAnimeFollow follow = getOne(new LambdaQueryWrapper<BizAnimeFollow>()
                .eq(BizAnimeFollow::getUserId, userId)
                .eq(BizAnimeFollow::getAnimeId, animeId));
        
        if (follow != null) {
            removeById(follow.getId());
            return false; // 已取消关注
        } else {
            follow = new BizAnimeFollow();
            follow.setUserId(userId);
            follow.setAnimeId(animeId);
            follow.setCreateTime(LocalDateTime.now());
            save(follow);
            return true; // 已关注
        }
    }

    @Override
    public List<BizAnime> getUserFollowList(Long userId) {
        List<BizAnimeFollow> follows = list(new LambdaQueryWrapper<BizAnimeFollow>()
                .eq(BizAnimeFollow::getUserId, userId)
                .orderByDesc(BizAnimeFollow::getCreateTime));
        
        List<Long> animeIds = follows.stream().map(BizAnimeFollow::getAnimeId).collect(Collectors.toList());
        if (animeIds.isEmpty()) {
            return List.of();
        }
        
        return animeService.listByIds(animeIds);
    }

    @Override
    public boolean isFollowed(Long userId, Long animeId) {
        if (userId == null) return false;
        return count(new LambdaQueryWrapper<BizAnimeFollow>()
                .eq(BizAnimeFollow::getUserId, userId)
                .eq(BizAnimeFollow::getAnimeId, animeId)) > 0;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean followByBangumi(Long userId, Integer bgmId) {
        // 1. 静默同步（如果已存在则返回现有对象）
        BizAnime anime = animeService.syncFromBangumi(bgmId);
        // 2. 调用现有关注逻辑
        return this.toggleFollow(userId, anime.getId());
    }

    @Override
    public boolean isFollowedByBgmId(Long userId, Integer bgmId) {
        if (userId == null) return false;
        // 先找本地是否有此 bgmId 的番剧
        BizAnime anime = animeService.getOne(new LambdaQueryWrapper<BizAnime>().eq(BizAnime::getBgmId, bgmId).last("LIMIT 1"));
        if (anime == null) return false;
        // 再检查是否关注
        return isFollowed(userId, anime.getId());
    }
}
