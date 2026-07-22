package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.domain.entity.BizUserFragment;
import com.ruoyi.project.mapper.BizUserFragmentMapper;
import com.ruoyi.project.service.IBizUserFragmentService;
import com.ruoyi.project.service.IBizUserPointsLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizUserFragmentServiceImpl extends ServiceImpl<BizUserFragmentMapper, BizUserFragment> implements IBizUserFragmentService {

    private final IBizUserPointsLogService pointsLogService;

    @Override
    public int getUserFragmentCount(Long userId) {
        BizUserFragment fragment = getOne(new LambdaQueryWrapper<BizUserFragment>()
                .eq(BizUserFragment::getUserId, userId)
                .eq(BizUserFragment::getFragmentType, "normal")
                .eq(BizUserFragment::getDelFlag, 0));
        return fragment != null ? fragment.getQuantity() : 0;
    }

    /**
     * 异步添加碎片（接口层，只负责异步调度）
     * <p>
     * 警告：@Async 和 @Transactional 不能放在同一个方法上。
     * 原因：@Async 在新线程执行，原线程的事务上下文不会传递，
     * 导致新线程中的 DB 操作没有事务保护。
     * 正确做法：@Async 只做调度 → 委托给带 @Transactional 的私有方法。
     * </p>
     */
    @Override
    @Async
    public void addFragment(Long userId, int count, String bizType, String bizRefId) {
        doAddFragment(userId, count, bizType, bizRefId);
    }

    /**
     * 实际的碎片添加逻辑（在新线程中拥有独立事务）
     */
    @Transactional(rollbackFor = Exception.class)
    void doAddFragment(Long userId, int count, String bizType, String bizRefId) {
        BizUserFragment fragment = getOne(new LambdaQueryWrapper<BizUserFragment>()
                .eq(BizUserFragment::getUserId, userId)
                .eq(BizUserFragment::getFragmentType, "normal")
                .eq(BizUserFragment::getDelFlag, 0));

        if (fragment == null) {
            fragment = new BizUserFragment();
            fragment.setUserId(userId);
            fragment.setFragmentType("normal");
            fragment.setQuantity(count);
            fragment.setCreateTime(LocalDateTime.now());
            fragment.setUpdateTime(LocalDateTime.now());
            fragment.setDelFlag(0);
            save(fragment);
        } else {
            fragment.setQuantity(fragment.getQuantity() + count);
            fragment.setUpdateTime(LocalDateTime.now());
            updateById(fragment);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean exchangeFragmentForPoints(Long userId, int fragmentCount) {
        if (fragmentCount < 100) {
            throw new RuntimeException("碎片数量不足100，无法兑换");
        }

        BizUserFragment fragment = getOne(new LambdaQueryWrapper<BizUserFragment>()
                .eq(BizUserFragment::getUserId, userId)
                .eq(BizUserFragment::getFragmentType, "normal")
                .eq(BizUserFragment::getDelFlag, 0));

        if (fragment == null || fragment.getQuantity() < fragmentCount) {
            throw new RuntimeException("碎片数量不足");
        }

        int pointsToAdd = (fragmentCount / 100) * 10;

        fragment.setQuantity(fragment.getQuantity() - fragmentCount);
        fragment.setUpdateTime(LocalDateTime.now());
        updateById(fragment);

        pointsLogService.addPoints(userId, pointsToAdd, "FRAGMENT_EXCHANGE", "fragment_" + System.currentTimeMillis());

        log.info("用户 {} 使用 {} 碎片兑换 {} 积分", userId, fragmentCount, pointsToAdd);
        return true;
    }
}
