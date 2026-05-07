package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizUserPointsLog;

public interface IBizUserPointsLogService extends IService<BizUserPointsLog> {
    
    /**
     * 增加评论积分
     * @param userId 用户ID
     * @param commentId 评论ID (用于防重)
     */
    void addPointsForComment(Long userId, Long commentId);
    
    /**
     * 获取用户积分
     * @param userId 用户ID
     * @return 用户当前积分
     */
    int getUserPoints(Long userId);
}
