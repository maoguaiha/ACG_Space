package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.dto.CommentReplyRequestDTO;
import com.ruoyi.project.domain.dto.CommentRequestDTO;
import com.ruoyi.project.domain.entity.BizComment;
import com.ruoyi.project.domain.vo.CommentPageItemVO;

import java.util.List;

public interface IBizCommentService extends IService<BizComment> {

    BizComment publishComment(CommentRequestDTO request);

    Page<CommentPageItemVO> pageByAnimeId(Long animeId, long pageNum, long pageSize);

    Page<CommentPageItemVO> pageAll(long pageNum, long pageSize, String keyword);

    boolean deleteComment(Long id);

    BizComment replyComment(CommentReplyRequestDTO request);

    List<CommentPageItemVO> getReplies(Long commentId);

    Page<CommentPageItemVO> pageByUserId(Long userId, long pageNum, long pageSize);
}
