package com.ruoyi.project.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 评论回复请求 DTO
 */
@Data
public class CommentReplyRequestDTO {

    @NotNull
    private Long commentId;

    @NotNull
    private Long userId;

    @NotBlank
    @Size(min = 1, max = 500)
    private String content;

    /** 回复目标用户ID（可选） */
    private Long replyToUserId;

    /** 回复目标用户昵称（前端展示用） */
    private String replyToNickname;
}
