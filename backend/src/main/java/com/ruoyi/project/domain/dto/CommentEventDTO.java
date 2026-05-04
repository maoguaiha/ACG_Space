package com.ruoyi.project.domain.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 评论事件传输对象
 */
@Data
public class CommentEventDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 番剧ID
     */
    private Long animeId;

    /**
     * 评论ID (作为业务唯一标识)
     */
    private Long commentId;
}
