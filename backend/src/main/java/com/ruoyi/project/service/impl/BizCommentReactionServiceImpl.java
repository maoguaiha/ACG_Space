package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.entity.BizComment;
import com.ruoyi.project.domain.entity.BizCommentReaction;
import com.ruoyi.project.mapper.BizCommentReactionMapper;
import com.ruoyi.project.service.IBizCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BizCommentReactionServiceImpl extends ServiceImpl<BizCommentReactionMapper, BizCommentReaction> {

    private final IBizCommentService commentService;

    @Transactional
    public void react(Long commentId, Integer reactionType) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) throw new RuntimeException("请先登录");

        // 查询旧反应
        LambdaQueryWrapper<BizCommentReaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizCommentReaction::getCommentId, commentId)
                .eq(BizCommentReaction::getUserId, userId);
        BizCommentReaction old = this.getOne(wrapper);

        // 相同类型 → 取消
        if (old != null && old.getReactionType().equals(reactionType)) {
            this.removeById(old.getId());
            updateLikesCount(commentId, reactionType, -1);
            return;
        }

        // 插入或更新
        BizCommentReaction reaction = new BizCommentReaction();
        reaction.setCommentId(commentId);
        reaction.setUserId(userId);
        reaction.setReactionType(reactionType);
        baseMapper.upsert(reaction);

        if (old != null) {
            updateLikesCount(commentId, old.getReactionType(), -1);
        }
        updateLikesCount(commentId, reactionType, 1);
    }

    private void updateLikesCount(Long commentId, Integer reactionType, int delta) {
        BizComment comment = commentService.getById(commentId);
        if (comment == null) return;
        if (reactionType == 1) {
            comment.setLikes((comment.getLikes() == null ? 0 : comment.getLikes()) + delta);
        } else if (reactionType == 2) {
            comment.setDislikes((comment.getDislikes() == null ? 0 : comment.getDislikes()) + delta);
        }
        commentService.updateById(comment);
    }

    /**
     * 获取用户对评论的反应状态
     * @return 1-已点赞, 2-已点踩, null-未反应
     */
    public Integer getReactionStatus(Long commentId, Long userId) {
        LambdaQueryWrapper<BizCommentReaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizCommentReaction::getCommentId, commentId)
                .eq(BizCommentReaction::getUserId, userId);
        BizCommentReaction reaction = this.getOne(wrapper);
        return reaction != null ? reaction.getReactionType() : null;
    }
}
