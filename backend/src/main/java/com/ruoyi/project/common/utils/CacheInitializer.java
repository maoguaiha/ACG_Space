package com.ruoyi.project.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.project.domain.entity.BizGachaPool;
import com.ruoyi.project.domain.entity.SysUser;
import com.ruoyi.project.service.IBizGachaPoolService;
import com.ruoyi.project.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 缓存初始化器
 * 启动时将数据库数据同步到Redis缓存
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInitializer implements ApplicationRunner {

    private final IBizGachaPoolService gachaPoolService;
    private final ISysUserService userService;
    private final LuaScriptExecutor luaScriptExecutor;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始初始化Redis缓存...");
        
        // 初始化奖池库存到Redis
        initGachaPoolStock();
        
        // 初始化用户积分到Redis
        initUserPoints();
        
        log.info("Redis缓存初始化完成");
    }

    /**
     * 初始化奖池库存
     */
    private void initGachaPoolStock() {
        List<BizGachaPool> pools = gachaPoolService.list(new LambdaQueryWrapper<BizGachaPool>()
                .eq(BizGachaPool::getDelFlag, 0));
        
        for (BizGachaPool pool : pools) {
            luaScriptExecutor.setPoolStock(pool.getId(), pool.getRemainingStock());
        }
        
        log.info("初始化奖池库存完成，共{}个奖池", pools.size());
    }

    /**
     * 初始化用户积分
     */
    private void initUserPoints() {
        List<SysUser> users = userService.list(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDelFlag, 0));
        
        for (SysUser user : users) {
            luaScriptExecutor.setUserPoints(user.getId(), user.getPoints());
        }
        
        log.info("初始化用户积分完成，共{}个用户", users.size());
    }
}
