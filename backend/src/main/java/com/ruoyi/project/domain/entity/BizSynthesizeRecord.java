package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 合成记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_synthesize_record")
public class BizSynthesizeRecord extends BaseEntity {

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
     * 配方ID
     */
    private Long recipeId;

    /**
     * 配方名称 (冗余)
     */
    private String recipeName;

    /**
     * 产物物品ID
     */
    private Long resultItemId;

    /**
     * 产物名称 (冗余)
     */
    private String resultItemName;

    /**
     * 产物数量
     */
    private Integer resultQuantity;

    /**
     * 消耗积分
     */
    private Integer costPoints;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 状态 (1=进行中 2=成功 3=失败)
     */
    private Integer status;
}