package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 博客文章实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_article")
public class BizArticle extends BaseEntity {

    /**
     * 主键ID (Snowflake)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 文章内容 (Markdown/富文本)
     */
    private String content;

    /**
     * 封面图片链接
     */
    private String coverUrl;

    /**
     * 作者用户ID
     */
    private Long authorId;

    /**
     * 文章分类
     */
    private String category;

    /**
     * 标签 (逗号分隔)
     */
    private String tags;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 点踩数
     */
    private Integer dislikeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 状态 (0草稿 1发布 2下架 3待审核 4驳回)
     */
    private Integer status;

    /**
     * 是否VIP专享 (0否 1是)
     */
    private Integer isVipOnly;

    /**
     * 是否推荐 (0否 1是)
     */
    private Integer isFeatured;

    /**
     * 驳回原因
     */
    private String rejectReason;
}
