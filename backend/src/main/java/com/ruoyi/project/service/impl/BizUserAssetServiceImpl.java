package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.domain.entity.BizUserAsset;
import com.ruoyi.project.mapper.BizUserAssetMapper;
import com.ruoyi.project.service.IBizUserAssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizUserAssetServiceImpl extends ServiceImpl<BizUserAssetMapper, BizUserAsset> implements IBizUserAssetService {

    @Override
    public Page<BizUserAsset> pageUserAssets(long pageNum, long pageSize, Long userId) {
        Page<BizUserAsset> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizUserAsset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizUserAsset::getDelFlag, 0);

        if (userId != null) {
            wrapper.eq(BizUserAsset::getUserId, userId);
        }

        wrapper.orderByDesc(BizUserAsset::getCreateTime);
        return this.page(page, wrapper);
    }
}
