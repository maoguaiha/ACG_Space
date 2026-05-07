package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizMarketItem;
import com.ruoyi.project.domain.vo.MarketItemVO;

import java.util.List;

/**
 * 市场服务接口
 */
public interface IBizMarketService extends IService<BizMarketItem> {

    /**
     * 分页查询市场商品列表
     */
    Page<MarketItemVO> pageItems(long pageNum, long pageSize, String itemName, String itemType,
                                  String rarity, Integer minPrice, Integer maxPrice, String sortBy);

    /**
     * 根据ID获取商品详情
     */
    MarketItemVO getItemById(Long id);

    /**
     * 创建市场挂单
     */
    Long listAsset(Long userId, Long assetId, Integer price);

    /**
     * 购买商品
     *
     * @param buyerId 买家ID
     * @param itemId 市场挂单ID
     * @return 订单号
     */
    String buyItem(Long buyerId, Long itemId);

    /**
     * 下架商品
     */
    boolean delistAsset(Long itemId, Long userId);

    /**
     * 获取用户的市场挂单列表
     */
    List<MarketItemVO> getUserListings(Long userId);
}