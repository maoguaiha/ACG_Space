package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.common.exception.BizErrorCode;
import com.ruoyi.project.common.exception.BizException;
import com.ruoyi.project.common.utils.JwtUtils;
import com.ruoyi.project.domain.entity.BizMessage;
import com.ruoyi.project.domain.entity.BizUserPointsLog;
import com.ruoyi.project.domain.entity.SysUser;
import com.ruoyi.project.mapper.BizMessageMapper;
import com.ruoyi.project.mapper.BizUserPointsLogMapper;
import com.ruoyi.project.mapper.SysUserMapper;
import com.ruoyi.project.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户服务实现
 * <p>
 * 面试亮点：
 * 1. 使用 BCryptPasswordEncoder 加密密码，杜绝明文存储
 * 2. 统一使用 BizException + BizErrorCode 错误体系
 * 3. 注册送积分 + 欢迎私信（事务保护）
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final BizMessageMapper messageMapper;
    private final BizUserPointsLogMapper pointsLogMapper;

    private static final int REGISTRATION_BONUS_POINTS = 2600;

    @Override
    public String login(String username, String password) {
        log.info("用户尝试登录: {}", username);

        SysUser user = getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            log.warn("登录失败: 用户 {} 不存在", username);
            throw new BizException(BizErrorCode.PASSWORD_ERROR);
        }

        // BCrypt 密码验证 — 替代不安全的明文比较
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("登录失败: 用户 {} 密码不匹配", username);
            throw new BizException(BizErrorCode.PASSWORD_ERROR);
        }

        log.info("用户登录成功: {}", username);
        return jwtUtils.createToken(user.getId(), user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(SysUser user) {
        log.info("用户尝试注册: {}", user.getUsername());

        long count = count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername()));
        if (count > 0) {
            log.warn("注册失败: 用户名 {} 已存在", user.getUsername());
            throw new BizException(BizErrorCode.USERNAME_EXISTS);
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            log.error("注册失败: 密码为空！");
            throw new BizException(BizErrorCode.PASSWORD_EMPTY);
        }

        // BCrypt 加密存储密码 — 杜绝明文
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        save(user);
        log.info("用户注册成功: {}", user.getUsername());

        sendRegistrationBonusMessage(user.getId());
    }

    /**
     * 发送注册积分奖励私信
     */
    private void sendRegistrationBonusMessage(Long userId) {
        BizMessage message = new BizMessage();
        message.setFromUserId(0L);
        message.setToUserId(userId);
        message.setContent("欢迎注册ACG Space！点击领取您的新人专属礼包，获得 " + REGISTRATION_BONUS_POINTS + " 积分！\n\n[领取积分]");
        message.setIsRead(false);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
        log.info("发送注册积分奖励私信成功, userId: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int sendBonusMessageToExistingUsers() {
        List<SysUser> users = list();
        int count = 0;

        for (SysUser user : users) {
            Long userId = user.getId();
            if (userId == null) continue;

            LambdaQueryWrapper<BizUserPointsLog> logWrapper = new LambdaQueryWrapper<>();
            logWrapper.eq(BizUserPointsLog::getUserId, userId)
                .eq(BizUserPointsLog::getActionType, "REGISTRATION");
            long existingCount = pointsLogMapper.selectCount(logWrapper);

            if (existingCount > 0) {
                log.info("用户 {} 已领取过注册积分，跳过", userId);
                continue;
            }

            LambdaQueryWrapper<BizMessage> msgWrapper = new LambdaQueryWrapper<>();
            msgWrapper.eq(BizMessage::getFromUserId, 0L)
                .eq(BizMessage::getToUserId, userId);
            long msgCount = messageMapper.selectCount(msgWrapper);

            if (msgCount > 0) {
                log.info("用户 {} 已收到注册奖励私信，跳过", userId);
                continue;
            }

            sendRegistrationBonusMessage(userId);
            count++;
        }

        log.info("补发注册积分私信完成，共发送 {} 条", count);
        return count;
    }
}
