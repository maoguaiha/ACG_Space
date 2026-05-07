package com.ruoyi.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.domain.entity.SysUser;
import com.ruoyi.project.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final ISysUserService sysUserService;

    @GetMapping("/page")
    public Result<Page<SysUser>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String nickname) {

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDelFlag, 0);
        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username);
        }
        if (StringUtils.hasText(nickname)) {
            wrapper.like(SysUser::getNickname, nickname);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> page = sysUserService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<SysUser> getDetail(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null || user.getDelFlag() != 0) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        if (user.getId() == null) {
            return Result.error("用户ID不能为空");
        }
        SysUser existUser = sysUserService.getById(user.getId());
        if (existUser == null || existUser.getDelFlag() != 0) {
            return Result.error("用户不存在");
        }
        sysUserService.updateById(user);
        return Result.success();
    }

    @DeleteMapping("/{ids}")
    public Result<Void> delete(@PathVariable String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .toList();
        for (Long id : idList) {
            SysUser user = new SysUser();
            user.setId(id);
            user.setDelFlag(2);
            sysUserService.updateById(user);
        }
        return Result.success();
    }

    @PutMapping("/status/{id}")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysUser user = sysUserService.getById(id);
        if (user == null || user.getDelFlag() != 0) {
            return Result.error("用户不存在");
        }
        user.setDelFlag(status == 0 ? 0 : 2);
        sysUserService.updateById(user);
        return Result.success();
    }

    @PutMapping("/vip/{id}")
    public Result<Void> updateVip(@PathVariable Long id, @RequestBody SysUser user) {
        SysUser existUser = sysUserService.getById(id);
        if (existUser == null || existUser.getDelFlag() != 0) {
            return Result.error("用户不存在");
        }
        if (user.getVipStatus() != null) {
            existUser.setVipStatus(user.getVipStatus());
        }
        if (user.getVipExpireTime() != null) {
            existUser.setVipExpireTime(user.getVipExpireTime());
        }
        if (user.getUserLevel() != null) {
            existUser.setUserLevel(user.getUserLevel());
        }
        if (user.getLevelExperience() != null) {
            existUser.setLevelExperience(user.getLevelExperience());
        }
        if (user.getPoints() != null) {
            existUser.setPoints(user.getPoints());
        }
        sysUserService.updateById(existUser);
        return Result.success();
    }
}