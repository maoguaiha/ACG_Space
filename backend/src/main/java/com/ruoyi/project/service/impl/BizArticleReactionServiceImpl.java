package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.entity.BizArticle;
import com.ruoyi.project.domain.entity.BizArticleReaction;
import com.ruoyi.project.mapper.BizArticleReactionMapper;
import com.ruoyi.project.service.IBizArticleReactionService;
import com.ruoyi.project.service.IBizArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BizArticleReactionServiceImpl extends ServiceImpl<BizArticleReactionMapper, BizArticleReaction> implements IBizArticleReactionService {

    private final IBizArticleService articleService;

    @Transactional
    public Integer react(Long articleId, Integer reactionType, String reason) {
        Long userId = SecurityUtils.getUserId();

        // 1. 使用 SQL 方式查询所有记录（包含已逻辑删除的）
        BizArticleReaction oldReaction = baseMapper.selectOneWithDeleted(articleId, userId);

        // 2. 如果存在记录（无论是否已删除），先物理删除
        if (oldReaction != null) {
            // 物理删除所有匹配的记录（包括已逻辑删除的）
            baseMapper.deletePhysically(articleId, userId);
            
            // 如果是已存在的有效反应，需要减少计数
            if (oldReaction.getDelFlag() == 0 && !oldReaction.getReactionType().equals(reactionType)) {
                updateArticleCount(articleId, oldReaction.getReactionType(), -1);
            }
            
            // 如果是相同类型，取消反应
            if (oldReaction.getDelFlag() == 0 && oldReaction.getReactionType().equals(reactionType)) {
                updateArticleCount(articleId, reactionType, -1);
                return null; // 取消后返回 null
            }
        }

        // 3. 插入新记录 — 使用 upsert 强制 del_flag = 0
        BizArticleReaction reaction = new BizArticleReaction();
        reaction.setArticleId(articleId);
        reaction.setUserId(userId);
        reaction.setReactionType(reactionType);
        reaction.setReason(reason);
        reaction.setDelFlag(0);
        baseMapper.upsert(reaction);

        // 4. 更新计数
        updateArticleCount(articleId, reactionType, 1);
        return reactionType; // 返回新的状态
    }

    private void updateArticleCount(Long articleId, Integer reactionType, int delta) {
        BizArticle article = articleService.getById(articleId);
        if (article == null) return;
        if (reactionType == 1) {
            article.setLikeCount((article.getLikeCount() == null ? 0 : article.getLikeCount()) + delta);
        } else if (reactionType == 2) {
            article.setDislikeCount((article.getDislikeCount() == null ? 0 : article.getDislikeCount()) + delta);
        }
        articleService.updateById(article);
    }

    /**
     * 获取用户对文章的反应状态
     * @return 1-已点赞, 2-已点踩, 0-未反应
     */
    public Integer getReactionStatus(Long articleId, Long userId) {
        BizArticleReaction reaction = baseMapper.selectOneWithDeleted(articleId, userId);
        System.out.println("[DEBUG selectOneWithDeleted] articleId=" + articleId + " userId=" + userId + " found=" + (reaction != null) + " reactionType=" + (reaction != null ? reaction.getReactionType() : "null"));
        return reaction != null ? reaction.getReactionType() : null;
    }
}
