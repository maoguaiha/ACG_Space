package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.common.utils.LuaScriptExecutor;
import com.ruoyi.project.domain.entity.BizUserPointsLog;
import com.ruoyi.project.mapper.BizUserPointsLogMapper;
import com.ruoyi.project.service.IBizUserPointsLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户积分日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizUserPointsLogServiceImpl extends ServiceImpl<BizUserPointsLogMapper, BizUserPointsLog>
        implements IBizUserPointsLogService {

    private final LuaScriptExecutor luaScriptExecutor;

    // 积分配置常量
    private static final int POINTS_FOR_COMMENT = 5;
    private static final int POINTS_FOR_LIKE = 2;
    private static final int POINTS_FOR_SIGN_IN = 10;
    private static final int POINTS_FOR_REGISTRATION = 2600;
    private static final int DAILY_SIGN_IN_LIMIT = 1;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPointsForComment(Long userId, Long commentId) {
        // 检查是否已发放过奖励（幂等性校验）
        String bizRefId = "COMMENT_" + commentId;
        if (checkExists("COMMENT", bizRefId)) {
            log.info("评论积分已发放, userId: {}, commentId: {}", userId, commentId);
            return;
        }

        // 添加积分
        luaScriptExecutor.setUserPoints(userId, luaScriptExecutor.getUserPoints(userId) + POINTS_FOR_COMMENT);

        // 记录日志
        BizUserPointsLog logEntry = createLogEntry(userId, "COMMENT", POINTS_FOR_COMMENT, bizRefId);
        this.save(logEntry);

        log.info("评论发奖成功, userId: {}, commentId: {}, points: {}", userId, commentId, POINTS_FOR_COMMENT);
    }

    /**
     * 点赞发奖
     *
     * @param userId 用户ID
     * @param likeId 点赞ID (用于防重)
     */
    @Transactional(rollbackFor = Exception.class)
    public void addPointsForLike(Long userId, Long likeId) {
        String bizRefId = "LIKE_" + likeId;
        if (checkExists("LIKE", bizRefId)) {
            log.info("点赞积分已发放, userId: {}, likeId: {}", userId, likeId);
            return;
        }

        luaScriptExecutor.setUserPoints(userId, luaScriptExecutor.getUserPoints(userId) + POINTS_FOR_LIKE);

        BizUserPointsLog logEntry = createLogEntry(userId, "LIKE", POINTS_FOR_LIKE, bizRefId);
        this.save(logEntry);

        log.info("点赞发奖成功, userId: {}, likeId: {}, points: {}", userId, likeId, POINTS_FOR_LIKE);
    }

    /**
     * 每日签到
     *
     * @param userId 用户ID
     * @return 是否签到成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean signIn(Long userId) {
        String today = LocalDate.now().toString();
        String bizRefId = "SIGN_IN_" + userId + "_" + today;

        if (checkExists("SIGN_IN", bizRefId)) {
            log.info("今日已签到, userId: {}", userId);
            return false;
        }

        luaScriptExecutor.setUserPoints(userId, luaScriptExecutor.getUserPoints(userId) + POINTS_FOR_SIGN_IN);

        BizUserPointsLog logEntry = createLogEntry(userId, "SIGN_IN", POINTS_FOR_SIGN_IN, bizRefId);
        this.save(logEntry);

        log.info("签到成功, userId: {}, points: {}", userId, POINTS_FOR_SIGN_IN);
        return true;
    }

    /**
     * 注册赠送积分
     *
     * @param userId 用户ID
     * @return 是否成功发放（true=首次领取成功，false=已领取过）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean awardRegistrationBonus(Long userId) {
        String bizRefId = "REGISTRATION_" + userId;
        log.info("开始处理注册积分领取, userId: {}", userId);

        if (checkExists("REGISTRATION", bizRefId)) {
            int currentPoints = luaScriptExecutor.getUserPoints(userId);
            log.info("注册积分已发放, userId: {}, 当前积分: {}", userId, currentPoints);
            return false;
        }

        int currentPoints = luaScriptExecutor.getUserPoints(userId);
        log.info("用户当前积分, userId: {}, currentPoints: {}", userId, currentPoints);
        
        int newPoints = currentPoints + POINTS_FOR_REGISTRATION;
        luaScriptExecutor.setUserPoints(userId, newPoints);
        log.info("设置用户积分成功, userId: {}, newPoints: {}", userId, newPoints);

        // 验证积分是否设置成功
        int verifyPoints = luaScriptExecutor.getUserPoints(userId);
        log.info("验证用户积分, userId: {}, verifyPoints: {}", userId, verifyPoints);

        BizUserPointsLog logEntry = createLogEntry(userId, "REGISTRATION", POINTS_FOR_REGISTRATION, bizRefId);
        try {
            this.save(logEntry);
            log.info("保存积分日志成功, userId: {}", userId);
        } catch (Exception e) {
            // 处理唯一键冲突（可能是并发领取导致）
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry") && e.getMessage().contains("uk_biz_ref")) {
                log.info("注册积分重复领取（并发）, userId: {}", userId);
                return false;
            }
            throw e;
        }

        log.info("注册赠送积分成功, userId: {}, points: {}", userId, POINTS_FOR_REGISTRATION);
        return true;
    }

    /**
     * 获取用户积分
     */
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
        int currentPoints = luaScriptExecutor.getUserPoints(userId);
        if (currentPoints < amount) {
            log.warn("积分不足, userId: {}, current: {}, needed: {}", userId, currentPoints, amount);
            return false;
        }

        luaScriptExecutor.setUserPoints(userId, currentPoints - amount);

        BizUserPointsLog logEntry = createLogEntry(userId, bizType, -amount, bizRefId);
        this.save(logEntry);

        log.info("扣减积分成功, userId: {}, amount: {}", userId, amount);
        return true;
    }

    /**
     * 检查是否已存在记录（幂等性校验）
     */
    private boolean checkExists(String actionType, String bizRefId) {
        return this.count(new LambdaQueryWrapper<BizUserPointsLog>()
                .eq(BizUserPointsLog::getActionType, actionType)
                .eq(BizUserPointsLog::getBizReferenceId, bizRefId)
                .eq(BizUserPointsLog::getDelFlag, 0)) > 0;
    }

    /**
     * 创建日志记录
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

    /**
     * 分页查询用户积分流水
     */
    public Page<BizUserPointsLog> getPointsLog(Long userId, long pageNum, long pageSize) {
        Page<BizUserPointsLog> page = new Page<>(pageNum, pageSize);
        return this.page(page, new LambdaQueryWrapper<BizUserPointsLog>()
                .eq(BizUserPointsLog::getUserId, userId)
                .eq(BizUserPointsLog::getDelFlag, 0)
                .orderByDesc(BizUserPointsLog::getCreateTime));
    }
}