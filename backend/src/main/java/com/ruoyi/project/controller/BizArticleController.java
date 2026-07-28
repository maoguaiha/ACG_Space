package com.ruoyi.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.entity.BizArticle;
import com.ruoyi.project.domain.vo.ArticleDetailVO;
import com.ruoyi.project.domain.vo.ArticleListVO;
import com.ruoyi.project.service.IBizArticleService;
import com.ruoyi.project.service.IBizMessageService;
import com.ruoyi.project.service.impl.BizArticleReactionServiceImpl;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
public class BizArticleController {

    private final IBizArticleService articleService;
    private final BizArticleReactionServiceImpl reactionService;
    private final IBizMessageService messageService;

    @GetMapping("/list")
    public Result<Page<ArticleListVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "default") String sortBy) {
        return Result.success(articleService.pageArticleList(pageNum, pageSize, keyword, category, status, sortBy));
    }

    /**
     * 获取文章分类列表
     */
    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        return Result.success(articleService.getAllCategories());
    }

    @GetMapping("/{id}")
    public Result<ArticleDetailVO> getDetail(@PathVariable Long id) {
        ArticleDetailVO detail = articleService.getArticleDetail(id);
        if (detail == null) {
            return Result.error("文章不存在");
        }
        return Result.success(detail);
    }

    @PostMapping
    public Result<BizArticle> create(@Validated @RequestBody BizArticle article) {
        BizArticle created = articleService.createArticle(article);
        return Result.success(created);
    }

    @PutMapping
    public Result<Void> update(@Validated @RequestBody BizArticle article) {
        articleService.updateArticle(article);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 先获取文章信息（作者、标题），删除后无法查询
        BizArticle article = articleService.getById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }
        boolean removed = articleService.deleteArticle(id);
        if (!removed) {
            return Result.error("删除失败");
        }
        // 发送删除通知
        messageService.sendSystemNotification(article.getAuthorId(),
                "您的文章《" + article.getTitle() + "》已被管理员删除");
        return Result.success(null);
    }

    @PutMapping("/featured/{id}")
    public Result<Void> toggleFeatured(@PathVariable Long id) {
        try {
            articleService.toggleFeatured(id);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/react")
    public Result<Integer> react(@PathVariable Long id, @RequestBody ReactionRequest request) {
        try {
            Integer newStatus = reactionService.react(id, request.getReactionType(), request.getReason());
            return Result.success(newStatus);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户对文章的反应状态
     */
    @GetMapping("/{id}/reaction-status")
    public Result<Integer> getReactionStatus(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.success(0);
        }
        Integer status = reactionService.getReactionStatus(id, userId);
        return Result.success(status == null ? 0 : status);
    }

    @Data
    public static class ReactionRequest {
        private Integer reactionType; // 1点赞 2点踩
        private String reason; // 点踩理由
    }
}
