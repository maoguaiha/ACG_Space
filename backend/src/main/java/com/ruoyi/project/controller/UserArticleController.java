package com.ruoyi.project.controller;

import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.domain.dto.ArticleCreateDTO;
import com.ruoyi.project.domain.entity.BizArticle;
import com.ruoyi.project.service.IBizArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户侧文章创建与我的文章接口骨架（Phase5）
 * 说明：实现为最小可用骨架，业务逻辑委托给现有 `IBizArticleService`。
 */
@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
public class UserArticleController {

    private final IBizArticleService articleService;

    @PostMapping("/create")
    public Result<Long> create(@Validated @RequestBody ArticleCreateDTO dto) {
        BizArticle article = new BizArticle();
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCoverUrl(dto.getCoverUrl());
        article.setCategory(dto.getCategory());
        article.setTags(dto.getTags());
        // 用户通过前端提交默认设为"待审核"(3)，dto 有值则用 dto 的值
        article.setStatus(dto.getStatus() != null ? dto.getStatus() : 3);
        BizArticle created = articleService.createArticle(article);
        return Result.success(created.getId());
    }

    @PostMapping("/submitReview/{id}")
    public Result<Void> submitReview(@PathVariable Long id) {
        BizArticle existing = articleService.getById(id);
        if (existing == null) {
            return Result.error("文章不存在");
        }
        Long userId = com.ruoyi.project.common.utils.SecurityUtils.getUserId();
        if (userId == null || !userId.equals(existing.getAuthorId())) {
            return Result.error("仅作者本人可提交审核");
        }
        existing.setStatus(3); // 待审核
        articleService.updateArticle(existing);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<Void> myArticles(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        // TODO: 从 articleService 查询当前用户的文章分页
        return Result.success();
    }
}
