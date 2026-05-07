package com.ruoyi.project.service;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizGachaPool;

public interface IBizGachaPoolService extends IService<BizGachaPool> {

    /**
     * 分页查询奖池列表
     */
    Page<BizGachaPool> pagePools(long pageNum, long pageSize, String name, Integer status);

    /**
     * 获取进行中的奖池列表
     */
    JSONArray getActivePools();

    /**
     * 创建奖池
     */
    boolean createPool(BizGachaPool pool);

    /**
     * 更新奖池
     */
    boolean updatePool(BizGachaPool pool);

    /**
     * 结束奖池
     */
    boolean endPool(Long id);

    /**
     * 检查奖池是否可抽
     */
    boolean isPoolAvailable(Long poolId);

    /**
     * 扣减奖池库存
     * @param poolId 奖池ID
     * @param count 扣减数量
     * @return 是否成功
     */
    boolean decrementStock(Long poolId, int count);
}