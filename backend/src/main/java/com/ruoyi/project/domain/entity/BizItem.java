package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物品/商品实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_item")
public class BizItem extends BaseEntity {

    /**
     * 主键ID (Snowflake)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 物品唯一标识 (如 item_ssr_001)
     */
    private String itemKey;

    /**
     * 物品名称
     */
    private String name;

    /**
     * 物品类型 (character/weapon/skin/material)
     */
    private String type;

    /**
     * 稀有度 (SSR/SR/R/N)
     */
    private String rarity;

    /**
     * 物品图片URL
     */
    private String image;

    /**
     * 物品描述
     */
    private String description;

    /**
     * 总库存
     */
    private Integer totalStock;

    /**
     * 剩余库存
     */
    private Integer remainingStock;

    /**
     * 参考价格(积分)
     */
    private Integer price;

    /**
     * 是否可上架市场 (0否 1是)
     */
    private Integer marketable;

    /**
     * 是否可合成 (0否 1是)
     */
    private Integer synthesizable;
}