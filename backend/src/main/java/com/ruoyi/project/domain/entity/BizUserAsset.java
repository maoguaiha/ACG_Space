package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户资产实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_user_asset")
public class BizUserAsset extends BaseEntity {

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
     * 物品ID
     */
    private Long itemId;

    /**
     * 资产唯一标识 (用户+物品组合)
     */
    private String assetKey;

    /**
     * 持有数量
     */
    private Integer quantity;

    /**
     * 状态 (1=正常 2=锁定 3=已使用 4=已合成)
     */
    private Integer status;

    /**
     * 是否实物 (0否 1是)
     */
    private Integer isPhysical;

    /**
     * 获取方式 (gacha/market/synthesize/gift)
     */
    private String acquireType;

    /**
     * 获取来源ID (抽赏记录ID/订单ID等)
     */
    private String acquireSourceId;

    /**
     * 是否已认证 (O2O核销需要)
     */
    private Integer isCertified;

    /**
     * 认证时间
     */
    private LocalDateTime certifiedTime;

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
     * 物品类型 (冗余)
     */
    private String itemType;
}