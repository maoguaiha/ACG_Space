package com.ruoyi.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.dto.ArticleCommentReplyRequestDTO;
import com.ruoyi.project.domain.dto.ArticleCommentRequestDTO;
import com.ruoyi.project.domain.entity.BizArticleComment;
import com.ruoyi.project.domain.vo.ArticleCommentVO;
import com.ruoyi.project.service.IBizArticleCommentService;
import com.ruoyi.project.service.impl.BizArticleCommentReactionServiceImpl;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/article/comment")
@RequiredArgsConstructor
public class BizArticleCommentController {

    private final IBizArticleCommentService articleCommentService;
    private final BizArticleCommentReactionServiceImpl reactionService;

    @PostMapping("/publish")
    public Result<BizArticleComment> publish(@Valid @RequestBody ArticleCommentRequestDTO requestDTO) {
        BizArticleComment comment = articleCommentService.publishComment(requestDTO);
        return Result.success(comment);
    }

    @GetMapping("/page")
    public Result<Page<ArticleCommentVO>> page(
            @RequestParam Long articleId,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(articleCommentService.pageByArticleId(articleId, pageNum, pageSize));
    }

    @PostMapping("/reply")
    public Result<BizArticleComment> reply(@Valid @RequestBody ArticleCommentReplyRequestDTO request) {
        BizArticleComment reply = articleCommentService.replyComment(request);
        return Result.success(reply);
    }

    @GetMapping("/{id}/replies")
    public Result<List<ArticleCommentVO>> getReplies(@PathVariable Long id) {
        return Result.success(articleCommentService.getReplies(id));
    }

    @PostMapping("/{id}/react")
    public Result<Integer> react(@PathVariable Long id, @RequestBody CommentReactionRequest request) {
        try {
            Integer newStatus = reactionService.react(id, request.getReactionType());
            return Result.success(newStatus);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户对评论的反应状态
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
    public static class CommentReactionRequest {
        private Integer reactionType;
    }
}