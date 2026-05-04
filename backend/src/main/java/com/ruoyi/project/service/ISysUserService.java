package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.SysUser;

/**
 * 系统用户服务接口
 */
public interface ISysUserService extends IService<SysUser> {
    
    /**
     * 登录
     */
    String login(String username, String password);

    /**
     * 注册
     */
    void register(SysUser user);
}
