package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizUserAsset;

public interface IBizUserAssetService extends IService<BizUserAsset> {

    Page<BizUserAsset> pageUserAssets(long pageNum, long pageSize, Long userId);
}
