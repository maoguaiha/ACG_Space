package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 抽赏记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_gacha_record")
public class BizGachaRecord extends BaseEntity {

    /**
     * 主键ID (Snowflake)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 奖池ID
     */
    private Long poolId;

    /**
     * 奖池名称 (冗余)
     */
    private String poolName;

    /**
     * 抽赏类型 (1=单抽 10=十连)
     */
    private Integer gachaType;

    /**
     * 消耗积分
     */
    private Integer costPoints;

    /**
     * 抽赏结果 (JSON数组)
     */
    private String resultItems;

    /**
     * 是否触发保底
     */
    private Integer isGuaranteed;

    /**
     * 关联交易ID
     */
    private String transactionId;

    /**
     * 状态 (1=成功 2=失败 3=退款)
     */
    private Integer status;
}