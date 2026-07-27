package com.ruoyi.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.dto.CommentReplyRequestDTO;
import com.ruoyi.project.domain.dto.CommentRequestDTO;
import com.ruoyi.project.domain.entity.BizComment;
import com.ruoyi.project.domain.vo.CommentPageItemVO;
import com.ruoyi.project.service.IBizCommentService;
import com.ruoyi.project.service.impl.BizCommentReactionServiceImpl;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class BizCommentController {

    private final IBizCommentService commentService;
    private final BizCommentReactionServiceImpl commentReactionService;

    @PostMapping("/publish")
    public Result<BizComment> publish(@Validated @RequestBody CommentRequestDTO requestDTO) {
        BizComment comment = commentService.publishComment(requestDTO);
        return Result.success(comment);
    }

    @GetMapping("/page")
    public Result<Page<CommentPageItemVO>> page(
            @RequestParam Long animeId,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(commentService.pageByAnimeId(animeId, pageNum, pageSize));
    }

    @GetMapping("/admin/page")
    public Result<Page<CommentPageItemVO>> adminPage(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(commentService.pageAll(pageNum, pageSize, keyword));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = commentService.deleteComment(id);
        return Result.success(success);
    }

    /**
     * 回复评论
     */
    @PostMapping("/reply")
    public Result<BizComment> reply(@Validated @RequestBody CommentReplyRequestDTO request) {
        BizComment reply = commentService.replyComment(request);
        return Result.success(reply);
    }

    /**
     * 获取某条评论下的所有回复
     */
    @GetMapping("/{id}/replies")
    public Result<List<CommentPageItemVO>> getReplies(@PathVariable Long id) {
        return Result.success(commentService.getReplies(id));
    }

    /**
     * 评论点赞/点踩
     */
    @PostMapping("/{id}/react")
    public Result<Void> react(@PathVariable Long id, @RequestBody CommentReactionRequest request) {
        try {
            commentReactionService.react(id, request.getReactionType());
            return Result.success(null);
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
        Integer status = commentReactionService.getReactionStatus(id, userId);
        return Result.success(status == null ? 0 : status);
    }

    @Data
    public static class CommentReactionRequest {
        private Integer reactionType;
    }
}
