package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充值订单实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_recharge_order")
public class BizRechargeOrder extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String orderNo;

    private Long userId;

    private BigDecimal amount;

    private Integer points;

    private Integer payStatus;

    private String payType;

    private LocalDateTime payTime;

    private String tradeNo;
}
