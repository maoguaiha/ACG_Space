package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 市场挂单实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_market_item")
public class BizMarketItem extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户资产ID (关联 biz_user_asset)
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
     * 挂单价格(积分)
     */
    private Integer price;

    /**
     * 状态 (0=待售 1=已售 2=已下架)
     */
    private Integer status;

    /**
     * 市场订单号
     */
    private String orderId;

    /**
     * 售出时间
     */
    private LocalDateTime soldTime;

    /**
     * 下架时间
     */
    private LocalDateTime delistTime;
}