package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.common.utils.JwtUtils;
import com.ruoyi.project.domain.entity.SysUser;
import com.ruoyi.project.mapper.SysUserMapper;
import com.ruoyi.project.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 系统用户服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final JwtUtils jwtUtils;

    @Override
    public String login(String username, String password) {
        log.info("用户尝试登录: {}", username);
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        
        if (user == null) {
            log.warn("登录失败: 用户 {} 不存在", username);
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (!user.getPassword().equals(password)) {
            log.warn("登录失败: 用户 {} 密码不匹配", username);
            throw new RuntimeException("用户名或密码错误");
        }
        
        log.info("用户登录成功: {}", username);
        return jwtUtils.createToken(user.getId(), user.getUsername());
    }

    @Override
    public void register(SysUser user) {
        log.info("用户尝试注册: {}", user.getUsername());
        long count = count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername()));
        if (count > 0) {
            log.warn("注册失败: 用户名 {} 已存在", user.getUsername());
            throw new RuntimeException("用户名已存在");
        }
        
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            log.error("注册失败: 密码为空！");
            throw new RuntimeException("密码不能为空");
        }
        
        save(user);
        log.info("用户注册成功: {}", user.getUsername());
    }
}
