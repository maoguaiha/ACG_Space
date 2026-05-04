package com.ruoyi.project.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论分页项视图对象
 */
@Data
public class CommentPageItemVO {

    private Long id;
    private Long animeId;
    private Long userId;
    private String content;
    private Long parentId;
    private Long replyToUserId;
    private String replyToNickname;
    private Integer likes;
    private LocalDateTime createTime;

    // 用户信息
    private String username;
    private String nickname;
    private String avatar;

    // 回复相关（仅主评论携带）
    private Integer replyCount;
    private List<CommentPageItemVO> replies;
}
