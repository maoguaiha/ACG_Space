package com.ruoyi.project.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全服务工具类
 */
public class SecurityUtils {

    /**
     * 获取用户ID
     **/
    public static Long getUserId() {
        try {
            return (Long) getAuthentication().getPrincipal();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取认证信息
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
