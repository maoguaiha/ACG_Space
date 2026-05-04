package com.ruoyi.project.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleListVO {

    private Long id;

    private String title;

    private String summary;

    private String coverUrl;

    private Long authorId;

    private String authorNickname;

    private String authorAvatar;

    private String category;

    private String tags;

    private Integer viewCount;

    private Integer likeCount;

    private Integer dislikeCount;

    private Integer commentCount;

    private Integer status;

    private Integer isVipOnly;

    private Integer isFeatured;

    private LocalDateTime createTime;
}