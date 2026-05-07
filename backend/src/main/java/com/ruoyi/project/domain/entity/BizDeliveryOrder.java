package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * O2O核销订单实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_delivery_order")
public class BizDeliveryOrder extends BaseEntity {

    /**
     * 主键ID (Snowflake)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 订单号 (DLV+时间戳+随机)
     */
    private String orderId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 资产ID
     */
    private Long assetId;

    /**
     * 物品ID
     */
    private Long itemId;

    /**
     * 物品名称 (冗余)
     */
    private String itemName;

    /**
     * 物品图片 (冗余)
     */
    private String itemImage;

    /**
     * 物品稀有度 (冗余)
     */
    private String itemRarity;

    /**
     * 收货人姓名
     */
    private String receiver;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 快递公司
     */
    private String expressCompany;

    /**
     * 快递单号
     */
    private String expressNo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态 (0=待发货 1=已发货 2=已完成 3=已取消)
     */
    private Integer status;

    /**
     * 发货时间
     */
    private LocalDateTime shipTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;
}