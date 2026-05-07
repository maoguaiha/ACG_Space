package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 番剧实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_anime")
public class BizAnime extends BaseEntity {

    /**
     * 主键ID (Snowflake)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * Bangumi 关联ID
     */
    private Integer bgmId;

    /**
     * 番剧名称
     */
    private String title;

    /**
     * 原版名称
     */
    private String titleOriginal;

    /**
     * 海报图片链接
     */
    private String coverUrl;

    /**
     * 剧情简介
     */
    private String summary;

    /**
     * 总集数
     */
    private Integer totalEpisodes;

    /**
     * 开播年份
     */
    private Integer publishYear;

    /**
     * 状态 (0连载中 1已完结 2未开播)
     */
    private Integer status;

    /**
     * 综合评分
     */
    private BigDecimal rating;

    /**
     * 番剧类型（逗号分隔，如：热血,异世界,治愈）
     */
    private String genre;

    /**
     * 是否首页轮播推荐（0否 1是）
     */
    private Integer featured;
}
