package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.common.utils.LuaScriptExecutor;
import com.ruoyi.project.domain.entity.BizUserPointsLog;
import com.ruoyi.project.domain.entity.SysUser;
import com.ruoyi.project.mapper.BizUserPointsLogMapper;
import com.ruoyi.project.mapper.SysUserMapper;
import com.ruoyi.project.service.IBizUserPointsLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户积分日志 Service 业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizUserPointsLogServiceImpl extends ServiceImpl<BizUserPointsLogMapper, BizUserPointsLog> implements IBizUserPointsLogService {

    private final LuaScriptExecutor luaScriptExecutor;
    private final SysUserMapper sysUserMapper;

    /**
     * 注册赠送积分（固定值）
     */
    private static final int POINTS_FOR_REGISTRATION = 2600;

    /**
     * 注册赠送积分
     *
     * @param userId 用户 ID
     * @return 是否成功发放（true=首次领取成功，false=已领取过）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean awardRegistrationBonus(Long userId) {
        String bizRefId = "REGISTRATION_" + userId;
        log.info("========== 开始处理注册积分领取，userId: {}, bizRefId: {} ==========", userId, bizRefId);

        // 1. 先尝试获取分布式锁（防止并发领取）
        String lockKey = "lock:registration:bonus:" + userId;
        Boolean locked = luaScriptExecutor.tryLock(lockKey, 10); // 10 秒超时
        if (locked == null || !locked) {
            log.warn("获取锁失败，可能正在领取中，userId: {}", userId);
            return false;
        }

        try {
            // 2. 在锁的保护下检查是否已领取
            boolean exists = checkExists("REGISTRATION", bizRefId);
            log.info("检查领取记录，userId: {}, exists: {}", userId, exists);
            
            if (exists) {
                int currentPoints = luaScriptExecutor.getUserPoints(userId);
                log.warn("注册积分已领取过，userId: {}, 当前积分：{}", userId, currentPoints);
                return false;
            }

            int currentPoints = luaScriptExecutor.getUserPoints(userId);
            log.info("用户当前积分,userId: {}, currentPoints: {}", userId, currentPoints);
            
            int newPoints = currentPoints + POINTS_FOR_REGISTRATION;
            luaScriptExecutor.setUserPoints(userId, newPoints);
            log.info("设置用户积分成功,userId: {}, newPoints: {}", userId, newPoints);

            // 同步更新数据库中的积分字段
            SysUser user = new SysUser();
            user.setId(userId);
            user.setPoints(newPoints);
            sysUserMapper.updateById(user);
            log.info("同步更新数据库积分成功,userId: {}, dbPoints: {}", userId, newPoints);

            // 验证积分是否设置成功
            int verifyPoints = luaScriptExecutor.getUserPoints(userId);
            log.info("验证用户积分，userId: {}, verifyPoints: {}", userId, verifyPoints);
            
            if (verifyPoints != newPoints) {
                log.error("积分设置验证失败！userId: {}, 期望：{}, 实际：{}", 
                        userId, newPoints, verifyPoints);
                throw new RuntimeException("积分设置验证失败");
            }

            BizUserPointsLog logEntry = createLogEntry(userId, "REGISTRATION", POINTS_FOR_REGISTRATION, bizRefId);
            this.save(logEntry);
            log.info("保存积分日志成功，userId: {}", userId);

            log.info("注册赠送积分成功，userId: {}, points: {}", userId, POINTS_FOR_REGISTRATION);
            return true;
        } finally {
            // 3. 释放锁
            luaScriptExecutor.unlock(lockKey);
            log.info("释放锁，userId: {}", userId);
        }
    }

    /**
     * 获取用户积分
     */
    @Override
    public int getUserPoints(Long userId) {
        return luaScriptExecutor.getUserPoints(userId);
    }

    /**
     * 设置用户积分
     */
    public void setUserPoints(Long userId, int points) {
        luaScriptExecutor.setUserPoints(userId, points);
    }

    /**
     * 扣减用户积分
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deductPoints(Long userId, int amount, String bizType, String bizRefId) {
        log.info("开始扣减用户积分，userId: {}, amount: {}, bizType: {}, bizRefId: {}", userId, amount, bizType, bizRefId);
        
        // 1. 检查积分是否充足
        int currentPoints = luaScriptExecutor.getUserPoints(userId);
        if (currentPoints < amount) {
            log.warn("积分不足，userId: {}, 当前积分：{}, 需要扣减：{}", userId, currentPoints, amount);
            return false;
        }

        // 2. 扣减积分
        int newPoints = currentPoints - amount;
        luaScriptExecutor.setUserPoints(userId, newPoints);
        log.info("扣减积分成功，userId: {}, 原积分：{}, 新积分：{}", userId, currentPoints, newPoints);

        // 3. 同步更新数据库中的积分字段
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPoints(newPoints);
        sysUserMapper.updateById(user);
        log.info("同步更新数据库积分成功，userId: {}, dbPoints: {}", userId, newPoints);

        // 4. 记录积分日志
        BizUserPointsLog logEntry = createLogEntry(userId, bizType, -amount, bizRefId);
        this.save(logEntry);
        log.info("扣减积分日志记录成功，userId: {}", userId);

        return true;
    }

    /**
     * 增加用户积分
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean addPoints(Long userId, int amount, String bizType, String bizRefId) {
        log.info("开始增加用户积分，userId: {}, amount: {}, bizType: {}, bizRefId: {}", userId, amount, bizType, bizRefId);
        
        // 1. 增加积分
        int currentPoints = luaScriptExecutor.getUserPoints(userId);
        int newPoints = currentPoints + amount;
        luaScriptExecutor.setUserPoints(userId, newPoints);
        log.info("增加积分成功，userId: {}, 原积分：{}, 新积分：{}", userId, currentPoints, newPoints);

        // 2. 同步更新数据库中的积分字段
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPoints(newPoints);
        sysUserMapper.updateById(user);
        log.info("同步更新数据库积分成功，userId: {}, dbPoints: {}", userId, newPoints);

        // 3. 记录积分日志
        BizUserPointsLog logEntry = createLogEntry(userId, bizType, amount, bizRefId);
        this.save(logEntry);
        log.info("增加积分日志记录成功，userId: {}", userId);

        return true;
    }

    /**
     * 增加评论积分
     * @param userId 用户 ID
     * @param commentId 评论 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void addPointsForComment(Long userId, Long commentId) {
        String bizRefId = "COMMENT_" + commentId;
        
        // 检查是否已领取过
        boolean exists = checkExists("COMMENT", bizRefId);
        if (exists) {
            log.warn("评论积分已领取过，userId: {}, commentId: {}", userId, commentId);
            return;
        }
        
        // 增加积分（假设评论积分是 10 分）
        addPoints(userId, 10, "COMMENT", bizRefId);
        log.info("评论积分领取成功，userId: {}, commentId: {}", userId, commentId);
    }

    /**
     * 增加点赞积分
     * @param userId 用户 ID
     * @param likeId 点赞目标 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void addPointsForLike(Long userId, Long likeId) {
        String bizRefId = "LIKE_" + likeId;
        
        // 检查是否已领取过
        boolean exists = checkExists("LIKE", bizRefId);
        if (exists) {
            log.warn("点赞积分已领取过，userId: {}, likeId: {}", userId, likeId);
            return;
        }
        
        // 增加积分（假设点赞积分是 5 分）
        addPoints(userId, 5, "LIKE", bizRefId);
        log.info("点赞积分领取成功，userId: {}, likeId: {}", userId, likeId);
    }

    /**
     * 每日签到
     * @param userId 用户 ID
     * @return true=签到成功，false=今日已签到
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean signIn(Long userId) {
        // 生成今日签到标识
        String bizRefId = "SIGNIN_" + java.time.LocalDate.now().toString();
        
        // 检查是否已签到
        boolean exists = checkExists("SIGNIN", bizRefId);
        if (exists) {
            log.warn("今日已签到，userId: {}", userId);
            return false;
        }
        
        // 增加签到积分（10 分）
        addPoints(userId, 10, "SIGNIN", bizRefId);
        log.info("签到成功，userId: {}, 获得 10 积分", userId);
        return true;
    }

    /**
     * 获取积分流水分页
     * @param userId 用户 ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<BizUserPointsLog> getPointsLog(
            Long userId, long pageNum, long pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<BizUserPointsLog> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<BizUserPointsLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizUserPointsLog::getUserId, userId)
               .orderByDesc(BizUserPointsLog::getCreateTime);
        
        return this.page(page, wrapper);
    }

    /**
     * 检查是否存在领取记录
     * @param actionType 操作类型
     * @param bizRefId 业务参考 ID
     * @return true=存在，false=不存在
     */
    private boolean checkExists(String actionType, String bizRefId) {
        LambdaQueryWrapper<BizUserPointsLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizUserPointsLog::getActionType, actionType)
               .eq(BizUserPointsLog::getBizReferenceId, bizRefId);
        
        Long count = this.count(wrapper);
        return count != null && count > 0;
    }

    /**
     * 创建积分日志对象
     * @param userId 用户 ID
     * @param actionType 操作类型
     * @param pointsChange 积分变化
     * @param bizRefId 业务参考 ID
     * @return 积分日志对象
     */
    private BizUserPointsLog createLogEntry(Long userId, String actionType, int pointsChange, String bizRefId) {
        BizUserPointsLog logEntry = new BizUserPointsLog();
        logEntry.setUserId(userId);
        logEntry.setActionType(actionType);
        logEntry.setPointsChange(pointsChange);
        logEntry.setBizReferenceId(bizRefId);
        logEntry.setDelFlag(0);
        logEntry.setCreateTime(LocalDateTime.now());
        logEntry.setUpdateTime(LocalDateTime.now());
        return logEntry;
    }
}
