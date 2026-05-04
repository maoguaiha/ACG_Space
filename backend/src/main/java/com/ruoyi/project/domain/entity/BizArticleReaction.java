package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_article_reaction")
public class BizArticleReaction extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long articleId;

    private Long userId;

    private Integer reactionType;

    private String reason;
}
