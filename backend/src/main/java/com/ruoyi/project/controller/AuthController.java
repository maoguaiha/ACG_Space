package com.ruoyi.project.controller;

import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.entity.SysUser;
import com.ruoyi.project.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ISysUserService userService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> loginForm) {
        String token = userService.login(loginForm.get("username"), loginForm.get("password"));
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        return Result.success(result);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody SysUser user) {
        userService.register(user);
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<SysUser> me() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(userService.getById(userId));
    }

    /**
     * 补发注册积分私信给所有已注册用户
     */
    @PostMapping("/send-bonus-to-existing")
    public Result<Map<String, Object>> sendBonusToExistingUsers() {
        int count = userService.sendBonusMessageToExistingUsers();
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("message", "补发完成，共发送 " + count + " 条私信");
        return Result.success(result);
    }
}
