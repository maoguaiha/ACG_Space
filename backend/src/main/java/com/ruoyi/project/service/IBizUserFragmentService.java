package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizUserFragment;

public interface IBizUserFragmentService extends IService<BizUserFragment> {

    int getUserFragmentCount(Long userId);

    boolean addFragment(Long userId, int count, String bizType, String bizRefId);

    boolean exchangeFragmentForPoints(Long userId, int fragmentCount);
}
