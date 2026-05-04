package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_article_comment_reaction")
public class BizArticleCommentReaction {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long articleCommentId;

    private Long userId;

    private Integer reactionType;

    private LocalDateTime createTime;
}
