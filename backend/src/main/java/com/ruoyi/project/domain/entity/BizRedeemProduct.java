package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 兑换实物商品实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_redeem_product")
public class BizRedeemProduct extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品图片
     */
    private String image;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 所需UR碎片数量
     */
    private Integer urFragmentCost;

    /**
     * 所需积分数量
     */
    private Integer pointsCost;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 已兑换数量
     */
    private Integer exchangedCount;

    /**
     * 状态 0-下架 1-上架
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sortOrder;
}
