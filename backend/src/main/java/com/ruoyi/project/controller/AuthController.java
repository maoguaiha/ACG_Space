package com.ruoyi.project.controller;

import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.entity.SysUser;
import com.ruoyi.project.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@Tag(name = "用户认证", description = "注册、登录、获取当前用户信息")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ISysUserService userService;

    /**
     * 登录
     */
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回 JWT Token")
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody @Validated LoginRequest loginForm) {
        String token = userService.login(loginForm.username(), loginForm.password());
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        return Result.success(result);
    }

    /**
     * 注册
     */
    @Operation(summary = "用户注册", description = "注册新用户，密码使用 BCrypt 加密存储")
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

    /**
     * 登录请求 DTO
     */
    @Schema(description = "登录请求")
    public record LoginRequest(
            @Schema(example = "admin") String username,
            @Schema(example = "admin123") String password) {}
}
