package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.entity.BizArticleComment;
import com.ruoyi.project.domain.entity.BizArticleCommentReaction;
import com.ruoyi.project.mapper.BizArticleCommentReactionMapper;
import com.ruoyi.project.service.IBizArticleCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BizArticleCommentReactionServiceImpl extends ServiceImpl<BizArticleCommentReactionMapper, BizArticleCommentReaction> {

    private final IBizArticleCommentService articleCommentService;

    @Transactional
    public Integer react(Long commentId, Integer reactionType) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) throw new RuntimeException("请先登录");

        LambdaQueryWrapper<BizArticleCommentReaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizArticleCommentReaction::getArticleCommentId, commentId)
            .eq(BizArticleCommentReaction::getUserId, userId);
        BizArticleCommentReaction old = this.getOne(wrapper);

        if (old != null && old.getReactionType().equals(reactionType)) {
            baseMapper.delete(wrapper); // 物理删除
            updateLikesCount(commentId, reactionType, -1);
            return null;
        }

        if (old != null) {
            baseMapper.delete(wrapper); // 物理删除
            updateLikesCount(commentId, old.getReactionType(), -1);
        }

        BizArticleCommentReaction reaction = new BizArticleCommentReaction();
        reaction.setArticleCommentId(commentId);
        reaction.setUserId(userId);
        reaction.setReactionType(reactionType);
        this.save(reaction);

        updateLikesCount(commentId, reactionType, 1);
        return reactionType;
    }

    private void updateLikesCount(Long commentId, Integer reactionType, int delta) {
        BizArticleComment comment = articleCommentService.getById(commentId);
        if (comment == null) return;
        if (reactionType == 1) {
            comment.setLikes((comment.getLikes() == null ? 0 : comment.getLikes()) + delta);
        } else if (reactionType == 2) {
            comment.setDislikes((comment.getDislikes() == null ? 0 : comment.getDislikes()) + delta);
        }
        articleCommentService.updateById(comment);
    }

    /**
     * 获取用户对评论的反应状态
     * @return 1-已点赞, 2-已点踩, null-未反应
     */
    public Integer getReactionStatus(Long commentId, Long userId) {
        LambdaQueryWrapper<BizArticleCommentReaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizArticleCommentReaction::getArticleCommentId, commentId)
                .eq(BizArticleCommentReaction::getUserId, userId);
        BizArticleCommentReaction reaction = this.getOne(wrapper);
        return reaction != null ? reaction.getReactionType() : null;
    }
}
