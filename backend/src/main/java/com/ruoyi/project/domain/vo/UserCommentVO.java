package com.ruoyi.project.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserCommentVO {
    private Long id;
    private String content;
    private Integer likes;
    private LocalDateTime createTime;
    // 评论类型：1-番剧评论，2-文章评论
    private Integer type;
    // 关联内容ID
    private Long targetId;
    // 关联内容标题
    private String targetTitle;
    // 关联内容封面
    private String targetCover;
}
