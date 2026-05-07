package com.ruoyi.project.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 市场商品视图对象
 */
@Data
public class MarketItemVO {

    /**
     * 挂单ID
     */
    private Long id;

    /**
     * 资产ID
     */
    private Long assetId;

    /**
     * 物品ID
     */
    private Long itemId;

    /**
     * 物品名称
     */
    private String itemName;

    /**
     * 物品图片
     */
    private String itemImage;

    /**
     * 物品稀有度
     */
    private String itemRarity;

    /**
     * 物品类型
     */
    private String itemType;

    /**
     * 卖家用户ID
     */
    private Long sellerId;

    /**
     * 卖家名称
     */
    private String sellerName;

    /**
     * 卖家头像
     */
    private String sellerAvatar;

    /**
     * 挂单价格(积分)
     */
    private Integer price;

    /**
     * 状态 (0=待售 1=已售 2=已下架)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private String createTime;
}