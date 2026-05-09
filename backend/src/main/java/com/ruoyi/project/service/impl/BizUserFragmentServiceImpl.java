package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.domain.entity.BizUserFragment;
import com.ruoyi.project.mapper.BizUserFragmentMapper;
import com.ruoyi.project.service.IBizUserFragmentService;
import com.ruoyi.project.service.IBizUserPointsLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addFragment(Long userId, int count, String bizType, String bizRefId) {
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
            return save(fragment);
        } else {
            fragment.setQuantity(fragment.getQuantity() + count);
            fragment.setUpdateTime(LocalDateTime.now());
            return updateById(fragment);
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
