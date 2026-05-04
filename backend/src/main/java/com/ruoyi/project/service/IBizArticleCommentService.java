package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.dto.ArticleCommentRequestDTO;
import com.ruoyi.project.domain.entity.BizArticleComment;
import com.ruoyi.project.domain.vo.ArticleCommentVO;

public interface IBizArticleCommentService extends IService<BizArticleComment> {

    BizArticleComment publishComment(ArticleCommentRequestDTO request);

    Page<ArticleCommentVO> pageByArticleId(Long articleId, long pageNum, long pageSize);

    BizArticleComment replyComment(com.ruoyi.project.domain.dto.ArticleCommentReplyRequestDTO request);

    java.util.List<ArticleCommentVO> getReplies(Long commentId);

    Page<ArticleCommentVO> pageByUserId(Long userId, long pageNum, long pageSize);
}