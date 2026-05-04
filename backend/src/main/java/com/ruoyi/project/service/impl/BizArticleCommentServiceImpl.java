package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.domain.dto.ArticleCommentReplyRequestDTO;
import com.ruoyi.project.domain.dto.ArticleCommentRequestDTO;
import com.ruoyi.project.domain.dto.MessageSendDTO;
import com.ruoyi.project.domain.entity.BizArticle;
import com.ruoyi.project.domain.entity.BizArticleComment;
import com.ruoyi.project.domain.entity.SysUser;
import com.ruoyi.project.domain.vo.ArticleCommentVO;
import com.ruoyi.project.mapper.BizArticleCommentMapper;
import com.ruoyi.project.service.IBizArticleCommentService;
import com.ruoyi.project.service.IBizArticleService;
import com.ruoyi.project.service.IBizMessageService;
import com.ruoyi.project.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizArticleCommentServiceImpl extends ServiceImpl<BizArticleCommentMapper, BizArticleComment> implements IBizArticleCommentService {

    private final ISysUserService sysUserService;
    private final IBizArticleService articleService;
    private final IBizMessageService messageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizArticleComment publishComment(ArticleCommentRequestDTO request) {
        BizArticleComment comment = new BizArticleComment();
        comment.setArticleId(request.getArticleId());
        comment.setUserId(request.getUserId());
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        comment.setLikes(0);
        this.save(comment);
        log.info("文章评论保存成功, commentId: {}", comment.getId());
        
        // 发送评论提示私信给文章作者
        sendCommentNotification(request.getArticleId(), request.getUserId(), null, comment.getContent());
        
        return comment;
    }

    @Override
    public Page<ArticleCommentVO> pageByArticleId(Long articleId, long pageNum, long pageSize) {
        Page<BizArticleComment> page = this.page(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BizArticleComment>()
                        .eq(BizArticleComment::getArticleId, articleId)
                        .eq(BizArticleComment::getDelFlag, 0)
                        .eq(BizArticleComment::getParentId, 0)
                        .orderByDesc(BizArticleComment::getCreateTime)
        );

        List<ArticleCommentVO> voList = buildCommentVOs(page.getRecords(), true);

        Page<ArticleCommentVO> result = new Page<>(pageNum, pageSize, page.getTotal());
        result.setPages(page.getPages());
        result.setRecords(voList);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizArticleComment replyComment(ArticleCommentReplyRequestDTO request) {
        BizArticleComment parent = this.getById(request.getCommentId());
        if (parent == null) {
            throw new RuntimeException("评论不存在");
        }
        BizArticleComment reply = new BizArticleComment();
        reply.setArticleId(parent.getArticleId());
        reply.setUserId(request.getUserId());
        reply.setContent(request.getContent());
        reply.setParentId(parent.getId());
        reply.setReplyToUserId(request.getReplyToUserId());
        reply.setReplyToNickname(request.getReplyToNickname());
        reply.setLikes(0);
        this.save(reply);
        log.info("文章评论回复成功, replyId: {}, parentId: {}", reply.getId(), parent.getId());
        
        // 发送回复提示私信给被回复的用户
        sendCommentNotification(parent.getArticleId(), request.getUserId(), request.getReplyToUserId(), reply.getContent());
        
        return reply;
    }

    @Override
    public List<ArticleCommentVO> getReplies(Long commentId) {
        List<BizArticleComment> replies = this.list(
                new LambdaQueryWrapper<BizArticleComment>()
                        .eq(BizArticleComment::getParentId, commentId)
                        .eq(BizArticleComment::getDelFlag, 0)
                        .orderByAsc(BizArticleComment::getCreateTime)
        );
        return buildCommentVOs(replies, false);
    }

    @Override
    public Page<ArticleCommentVO> pageByUserId(Long userId, long pageNum, long pageSize) {
        Page<BizArticleComment> page = this.page(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BizArticleComment>()
                        .eq(BizArticleComment::getUserId, userId)
                        .eq(BizArticleComment::getDelFlag, 0)
                        .orderByDesc(BizArticleComment::getCreateTime)
        );
        List<ArticleCommentVO> voList = buildCommentVOs(page.getRecords(), false);
        Page<ArticleCommentVO> result = new Page<>(pageNum, pageSize, page.getTotal());
        result.setPages(page.getPages());
        result.setRecords(voList);
        return result;
    }

    // ========== 私有方法 ==========

    private List<ArticleCommentVO> buildCommentVOs(List<BizArticleComment> comments, boolean withReplies) {
        if (comments.isEmpty()) return Collections.emptyList();

        Set<Long> userIds = comments.stream().map(BizArticleComment::getUserId).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = userIds.isEmpty() ? Collections.emptyMap()
                : sysUserService.listByIds(userIds).stream().collect(Collectors.toMap(SysUser::getId, u -> u));

        return comments.stream().map(comment -> {
            ArticleCommentVO vo = toVO(comment, userMap);
            if (withReplies && comment.getParentId() == 0) {
                List<BizArticleComment> replyList = this.list(
                        new LambdaQueryWrapper<BizArticleComment>()
                                .eq(BizArticleComment::getParentId, comment.getId())
                                .eq(BizArticleComment::getDelFlag, 0)
                                .orderByAsc(BizArticleComment::getCreateTime)
                                .last("LIMIT 3")
                );
                Set<Long> replyUserIds = replyList.stream().map(BizArticleComment::getUserId).collect(Collectors.toSet());
                Map<Long, SysUser> replyUserMap = replyUserIds.isEmpty() ? Collections.emptyMap()
                        : sysUserService.listByIds(replyUserIds).stream().collect(Collectors.toMap(SysUser::getId, u -> u));
                vo.setReplies(replyList.stream().map(r -> toVO(r, replyUserMap)).toList());
                long totalReplies = this.count(new LambdaQueryWrapper<BizArticleComment>()
                        .eq(BizArticleComment::getParentId, comment.getId())
                        .eq(BizArticleComment::getDelFlag, 0));
                vo.setReplyCount((int) totalReplies);
            }
            return vo;
        }).toList();
    }

    private ArticleCommentVO toVO(BizArticleComment comment, Map<Long, SysUser> userMap) {
        ArticleCommentVO vo = new ArticleCommentVO();
        vo.setId(comment.getId());
        vo.setArticleId(comment.getArticleId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setParentId(comment.getParentId());
        vo.setReplyToUserId(comment.getReplyToUserId());
        vo.setReplyToNickname(comment.getReplyToNickname());
        vo.setLikes(comment.getLikes());
        vo.setDislikes(comment.getDislikes());
        vo.setCreateTime(comment.getCreateTime());
        SysUser user = userMap.get(comment.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }
        return vo;
    }
    
    /**
     * 发送评论提示私信
     * @param articleId 文章ID
     * @param commentUserId 评论用户ID
     * @param replyToUserId 被回复用户ID（回复时使用）
     * @param content 评论内容
     */
    private void sendCommentNotification(Long articleId, Long commentUserId, Long replyToUserId, String content) {
        try {
            SysUser commentUser = sysUserService.getById(commentUserId);
            if (commentUser == null) return;
            
            String commentUserName = commentUser.getNickname() != null ? commentUser.getNickname() : commentUser.getUsername();
            
            // 获取文章信息
            BizArticle article = articleService.getById(articleId);
            if (article == null) return;
            
            Long targetUserId;
            String messageContent;
            
            if (replyToUserId != null && !replyToUserId.equals(commentUserId)) {
                // 回复评论，发送给被回复的用户
                targetUserId = replyToUserId;
                messageContent = String.format("%s 回复了你的评论：%s", commentUserName, content.length() > 50 ? content.substring(0, 50) + "..." : content);
            } else {
                // 新评论，发送给文章作者
                targetUserId = article.getAuthorId();
                if (targetUserId == null || targetUserId.equals(commentUserId)) {
                    return; // 作者给自己评论不发送通知
                }
                messageContent = String.format("%s 在你的文章「%s」下发表了评论：%s", 
                        commentUserName, 
                        article.getTitle() != null ? article.getTitle() : "未知文章",
                        content.length() > 50 ? content.substring(0, 50) + "..." : content);
            }
            
            MessageSendDTO dto = new MessageSendDTO();
            dto.setToUserId(targetUserId);
            dto.setContent(messageContent);
            messageService.sendMessage(dto);
            log.info("评论提示私信已发送，fromUserId: {}, toUserId: {}", commentUserId, targetUserId);
            
        } catch (Exception e) {
            log.warn("发送评论提示私信失败", e);
        }
    }
}