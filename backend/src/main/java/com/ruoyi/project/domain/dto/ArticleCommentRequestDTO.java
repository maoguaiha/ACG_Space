package com.ruoyi.project.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArticleCommentRequestDTO {
    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Long parentId;
}