package com.ruoyi.project.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class ArticleCreateDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String summary;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String coverUrl;

    private String category;

    private String tags;

    // status: 0=草稿,1=已发布,3=待审核
    private Integer status = 0;
}
