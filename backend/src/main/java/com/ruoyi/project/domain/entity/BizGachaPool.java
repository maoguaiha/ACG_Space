package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 抽赏奖池实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_gacha_pool")
public class BizGachaPool extends BaseEntity {

    /**
     * 主键ID (Snowflake)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 奖池名称
     */
    private String name;

    /**
     * 奖池描述
     */
    private String description;

    /**
     * 奖池Banner图片URL
     */
    private String banner;

    /**
     * 限定稀有度 (SSR/SR/normal)
     */
    private String rarity;

    /**
     * 奖池总库存
     */
    private Integer totalStock;

    /**
     * 奖池剩余库存
     */
    private Integer remainingStock;

    /**
     * 单抽价格(积分)
     */
    private Integer singleCost;

    /**
     * 十连价格(积分)
     */
    private Integer tenCost;

    /**
     * 保底次数 (多少抽必出SSR)
     */
    private Integer guaranteeCount;

    /**
     * 保底类型 (rarity=稀有度保底 count=次数保底)
     */
    private String guaranteeType;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态 (0=未开始 1=进行中 2=已结束)
     */
    private Integer status;

    /**
     * 权重配置 (JSON格式)
     */
    private String weightConfig;
}