package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 抽赏奖池物品配置实体类
 */
@Data
@TableName("biz_gacha_pool_item")
public class BizGachaPoolItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 奖池ID
     */
    private Long poolId;

    /**
     * 物品ID
     */
    private Long itemId;

    /**
     * 物品稀有度
     */
    private String rarity;

    /**
     * 权重
     */
    private Integer weight;

    /**
     * 是否保底物品 (0否 1是)
     */
    private Integer isGuarantee;

    /**
     * 库存上限 (NULL表示不限)
     */
    private Integer stockLimit;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @TableLogic(value = "0", delval = "2")
    private Integer delFlag;
}