package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 交易订单实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_transaction")
public class BizTransaction extends BaseEntity {

    /**
     * 主键ID (Snowflake)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 订单号 (TXN+时间戳+随机)
     */
    private String orderId;

    /**
     * 买家用户ID
     */
    private Long buyerId;

    /**
     * 卖家用户ID
     */
    private Long sellerId;

    /**
     * 资产ID (用户资产表)
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
     * 交易金额(积分)
     */
    private Integer amount;

    /**
     * 手续费(积分, 1%)
     */
    private Integer fee;

    /**
     * 卖家实得(积分)
     */
    private Integer sellerAmount;

    /**
     * 状态 (0=处理中 1=成功 2=失败 3=回查中)
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * RocketMQ事务ID
     */
    private String rocketmqTxId;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;
}