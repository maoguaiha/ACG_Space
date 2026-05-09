package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 兑换订单实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_redeem_order")
public class BizRedeemOrder extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long assetId;

    private Long itemId;

    private String itemName;

    private String itemImage;

    private String itemRarity;

    private Long productId;

    private String productName;

    private String productImage;

    private Integer urFragmentCost;

    private Integer pointsCost;

    private String receiver;

    private String phone;

    private String province;

    private String city;

    private String district;

    private String address;

    private Integer status;

    private String logisticsCompany;

    private String logisticsNo;

    private LocalDateTime shipTime;

    private LocalDateTime completeTime;
}
