package com.ruoyi.project.config;

import com.ruoyi.project.domain.entity.SysUser;
import com.ruoyi.project.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 种子数据初始化器
 * <p>
 * 启动时检查数据库是否有用户，无则创建默认账号。
 * 仅在 dev 环境生效（生产环境应使用外部种子脚本）。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        long count = sysUserMapper.selectCount(null);
        if (count > 0) {
            log.info("数据库已有 {} 个用户，跳过种子数据初始化", count);
            return;
        }

        log.info("数据库无用户，开始创建默认账号...");

        createUser("admin", "管理员", "admin123", 99999);
        createUser("test", "测试用户", "test123", 10000);

        log.info("默认账号创建完成: admin/admin123, test/test123");
    }

    private void createUser(String username, String nickname, String plainPassword, int points) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setNickname(nickname);
        user.setPassword(passwordEncoder.encode(plainPassword));
        user.setAvatar("https://picsum.photos/seed/" + username + "/200/200");
        user.setEmail(username + "@acgspace.dev");
        user.setPoints(points);
        user.setUserLevel(1);
        user.setLevelExperience(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDelFlag(0);
        sysUserMapper.insert(user);
        log.info("创建用户: {} (id={})", username, user.getId());
    }
}
