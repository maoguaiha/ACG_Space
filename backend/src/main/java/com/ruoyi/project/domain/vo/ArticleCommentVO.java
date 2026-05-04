package com.ruoyi.project.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleCommentVO {
    private Long id;
    private Long articleId;
    private Long userId;
    private String content;
    private Long parentId;
    private Long replyToUserId;
    private String replyToNickname;
    private Integer likes;
    private Integer dislikes;
    private LocalDateTime createTime;

    // 用户信息
    private String username;
    private String nickname;
    private String avatar;

    // 回复相关
    private Integer replyCount;
    private List<ArticleCommentVO> replies;
}