package com.ruoyi.project.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.domain.entity.BizGachaPool;
import com.ruoyi.project.mapper.BizGachaPoolMapper;
import com.ruoyi.project.service.IBizGachaPoolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizGachaPoolServiceImpl extends ServiceImpl<BizGachaPoolMapper, BizGachaPool> implements IBizGachaPoolService {

    @Override
    public Page<BizGachaPool> pagePools(long pageNum, long pageSize, String name, Integer status) {
        Page<BizGachaPool> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizGachaPool> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizGachaPool::getDelFlag, 0);

        if (name != null && !name.isBlank()) {
            wrapper.like(BizGachaPool::getName, name);
        }
        if (status != null) {
            wrapper.eq(BizGachaPool::getStatus, status);
        }

        wrapper.orderByDesc(BizGachaPool::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public JSONArray getActivePools() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<BizGachaPool> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizGachaPool::getDelFlag, 0)
                .eq(BizGachaPool::getStatus, 1)
                .le(BizGachaPool::getStartTime, now)
                .and(w -> w.isNull(BizGachaPool::getEndTime).or()
                        .ge(BizGachaPool::getEndTime, now))
                .gt(BizGachaPool::getRemainingStock, 0)
                .orderByDesc(BizGachaPool::getCreateTime);

        List<BizGachaPool> pools = this.list(wrapper);

        LambdaQueryWrapper<BizGachaPool> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(BizGachaPool::getDelFlag, 0)
                .eq(BizGachaPool::getStatus, 0)
                .le(BizGachaPool::getStartTime, now)
                .and(w -> w.isNull(BizGachaPool::getEndTime).or()
                        .ge(BizGachaPool::getEndTime, now))
                .gt(BizGachaPool::getRemainingStock, 0);

        List<BizGachaPool> pendingPools = this.list(pendingWrapper);
        for (BizGachaPool pool : pendingPools) {
            pool.setStatus(1);
            pool.setUpdateTime(LocalDateTime.now());
            this.updateById(pool);
            pools.add(pool);
        }

        pools.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));

        return JSON.parseArray(JSON.toJSONString(pools));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createPool(BizGachaPool pool) {
        pool.setDelFlag(0);
        pool.setStatus(0);
        pool.setRemainingStock(pool.getTotalStock());
        pool.setCreateTime(LocalDateTime.now());
        pool.setUpdateTime(LocalDateTime.now());
        return this.save(pool);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePool(BizGachaPool pool) {
        pool.setUpdateTime(LocalDateTime.now());
        return this.updateById(pool);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean endPool(Long id) {
        BizGachaPool pool = this.getById(id);
        if (pool == null) {
            return false;
        }
        pool.setStatus(2);
        pool.setUpdateTime(LocalDateTime.now());
        return this.updateById(pool);
    }

    @Override
    public boolean isPoolAvailable(Long poolId) {
        BizGachaPool pool = this.getById(poolId);
        if (pool == null || pool.getDelFlag() != 0) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        if (pool.getStatus() != 1) {
            return false;
        }

        if (pool.getStartTime() != null && pool.getStartTime().isAfter(now)) {
            return false;
        }

        if (pool.getEndTime() != null && pool.getEndTime().isBefore(now)) {
            return false;
        }

        if (pool.getRemainingStock() == null || pool.getRemainingStock() <= 0) {
            return false;
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decrementStock(Long poolId, int count) {
        BizGachaPool pool = this.getById(poolId);
        if (pool == null) {
            return false;
        }

        if (pool.getRemainingStock() == null || pool.getRemainingStock() < count) {
            log.warn("奖池库存不足, poolId: {}, remainingStock: {}, need: {}",
                    poolId, pool.getRemainingStock(), count);
            return false;
        }

        pool.setRemainingStock(pool.getRemainingStock() - count);
        pool.setUpdateTime(LocalDateTime.now());
        return this.updateById(pool);
    }
}