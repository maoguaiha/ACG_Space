package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizAnimeFollow;
import com.ruoyi.project.domain.entity.BizAnime;

import java.util.List;

/**
 * 追番记录服务接口
 */
public interface IBizAnimeFollowService extends IService<BizAnimeFollow> {
    
    /**
     * 切换追番状态
     */
    boolean toggleFollow(Long userId, Long animeId);

    /**
     * 获取用户的追番列表
     */
    List<BizAnime> getUserFollowList(Long userId);

    /**
     * 检查是否已追番
     */
    boolean isFollowed(Long userId, Long animeId);

    /**
     * 根据 Bangumi ID 追番（如果不存在则自动同步）
     */
    boolean followByBangumi(Long userId, Integer bgmId);

    /**
     * 根据 Bangumi ID 检查是否已追番
     */
    boolean isFollowedByBgmId(Long userId, Integer bgmId);
}
