package com.ruoyi.project.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 交易事件 DTO
 * 用于 RocketMQ 事务消息传递
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEventDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单号
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
     * 交易金额(积分)
     */
    private Integer amount;

    /**
     * 手续费(积分)
     */
    private Integer fee;

    /**
     * 卖家实得(积分)
     */
    private Integer sellerAmount;

    /**
     * 事件类型：asset_transfer=资产转移 points_transfer=积分转移
     */
    private String eventType;
}