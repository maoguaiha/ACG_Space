package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 番剧评论互动实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_comment")
public class BizComment extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属番剧ID
     */
    private Long animeId;

    /**
     * 发布用户ID
     */
    private Long userId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID (回复树)，默认为0
     */
    private Long parentId;

    /**
     * 回复目标用户ID
     */
    private Long replyToUserId;

    /**
     * 回复目标用户昵称
     */
    private String replyToNickname;

    /**
     * 点赞数
     */
    private Integer likes;

    /**
     * 点踩数
     */
    private Integer dislikes;
}
