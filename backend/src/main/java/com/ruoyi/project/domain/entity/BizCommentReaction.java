package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_comment_reaction")
public class BizCommentReaction {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long commentId;

    private Long userId;

    private Integer reactionType;

    private LocalDateTime createTime;
}
