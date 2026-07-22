package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizUserFragment;

public interface IBizUserFragmentService extends IService<BizUserFragment> {

    int getUserFragmentCount(Long userId);

    /**
     * 添加碎片（异步执行，不阻塞调用方）
     * <p>调用方不依赖此方法的返回结果，碎片写入在后台上报即可。</p>
     */
    void addFragment(Long userId, int count, String bizType, String bizRefId);

    boolean exchangeFragmentForPoints(Long userId, int fragmentCount);
}
