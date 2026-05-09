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

    /**
     * 扣减用户积分
     * @param userId 用户ID
     * @param amount 扣减数量
     * @param bizType 业务类型
     * @param bizRefId 业务参考ID
     * @return 是否成功
     */
    boolean deductPoints(Long userId, int amount, String bizType, String bizRefId);

    /**
     * 增加用户积分
     * @param userId 用户ID
     * @param amount 增加数量
     * @param bizType 业务类型
     * @param bizRefId 业务参考ID
     * @return 是否成功
     */
    boolean addPoints(Long userId, int amount, String bizType, String bizRefId);

    /**
     * 注册赠送积分
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean awardRegistrationBonus(Long userId);

    /**
     * 每日签到
     * @param userId 用户ID
     * @return true=签到成功，false=今日已签到
     */
    boolean signIn(Long userId);
}
