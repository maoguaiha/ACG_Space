package com.ruoyi.project.domain.dto;

import lombok.Data;

@Data
public class ArticleReviewRequestDTO {
    private Long id;
    private Boolean approve;
    private String rejectReason;
}
