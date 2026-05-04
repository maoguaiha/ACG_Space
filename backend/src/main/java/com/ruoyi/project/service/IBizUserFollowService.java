package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizUserFollow;

/**
 * 用户关注服务接口
 */
public interface IBizUserFollowService extends IService<BizUserFollow> {

    /**
     * 关注/取关用户（toggle）
     * @return true=已关注, false=已取消关注
     */
    boolean toggleFollow(Long targetUserId);

    /**
     * 是否已关注
     */
    boolean isFollowing(Long userId, Long targetUserId);

    /**
     * 获取粉丝数
     */
    long getFollowerCount(Long userId);

    /**
     * 获取关注数
     */
    long getFollowingCount(Long userId);
}
