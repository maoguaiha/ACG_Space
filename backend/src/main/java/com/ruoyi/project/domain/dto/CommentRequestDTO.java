package com.ruoyi.project.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评论请求参数
 */
@Data
public class CommentRequestDTO {

    @NotNull(message = "番剧ID不能为空")
    private Long animeId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private Long parentId = 0L;

    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 500, message = "评论内容长度必须在 1 到 500 个字符之间")
    private String content;
}
