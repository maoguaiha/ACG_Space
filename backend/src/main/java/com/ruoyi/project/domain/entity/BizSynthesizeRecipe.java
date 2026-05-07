package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 合成配方实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_synthesize_recipe")
public class BizSynthesizeRecipe extends BaseEntity {

    /**
     * 主键ID (Snowflake)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 配方名称
     */
    private String name;

    /**
     * 配方描述
     */
    private String description;

    /**
     * 产物物品ID
     */
    private Long resultItemId;

    /**
     * 产物数量
     */
    private Integer resultQuantity;

    /**
     * 消耗类型 (materials=材料消耗 items=指定物品消耗)
     */
    private String costType;

    /**
     * 消耗材料配置 (JSON)
     */
    private String costItems;

    /**
     * 额外消耗积分
     */
    private Integer costPoints;

    /**
     * 成功率 (%)
     */
    private Integer successRate;

    /**
     * 状态 (0=禁用 1=启用)
     */
    private Integer status;
}