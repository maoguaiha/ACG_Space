package com.ruoyi.project.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.common.constant.MqConstants;
import com.ruoyi.project.domain.dto.CommentEventDTO;
import com.ruoyi.project.domain.dto.CommentReplyRequestDTO;
import com.ruoyi.project.domain.dto.CommentRequestDTO;
import com.ruoyi.project.domain.dto.MessageSendDTO;
import com.ruoyi.project.domain.entity.BizComment;
import com.ruoyi.project.domain.entity.SysUser;
import com.ruoyi.project.domain.vo.CommentPageItemVO;
import com.ruoyi.project.mapper.BizCommentMapper;
import com.ruoyi.project.service.IBizCommentService;
import com.ruoyi.project.service.IBizMessageService;
import com.ruoyi.project.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizCommentServiceImpl extends ServiceImpl<BizCommentMapper, BizComment> implements IBizCommentService {

    private final RocketMQTemplate rocketMQTemplate;
    private final ISysUserService sysUserService;
    private final IBizMessageService messageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizComment publishComment(CommentRequestDTO request) {
        // 1. 保存评论到数据库 (同步响应)
        BizComment comment = new BizComment();
        comment.setAnimeId(request.getAnimeId());
        comment.setUserId(request.getUserId());
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        comment.setLikes(0);
        this.save(comment);

        log.info("评论保存成功, commentId: {}", comment.getId());

        // 2. 组装 MQ 消息体
        CommentEventDTO eventDTO = new CommentEventDTO();
        eventDTO.setUserId(comment.getUserId());
        eventDTO.setAnimeId(comment.getAnimeId());
        eventDTO.setCommentId(comment.getId());

        // 3. 异步发送 MQ 消息触发积分结算
        try {
            rocketMQTemplate.asyncSend(MqConstants.TOPIC_COMMENT_EVENT, MessageBuilder.withPayload(JSON.toJSONString(eventDTO)).build(), new org.apache.rocketmq.client.producer.SendCallback() {
                @Override
                public void onSuccess(org.apache.rocketmq.client.producer.SendResult sendResult) {
                    log.info("评论积分事件发送 MQ 成功: {}", sendResult.getMsgId());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("评论积分事件发送 MQ 失败", e);
                    // 业务补偿逻辑可以写在这里
                }
            });
        } catch (Exception e) {
            log.error("MQ 生产者发送异常", e);
        }

        // 4. 发送评论提示私信（如果是回复）
        if (request.getParentId() != null && request.getParentId() > 0) {
            sendAnimeCommentReplyNotification(request.getParentId(), request.getUserId(), comment.getContent());
        }

        return comment;
    }

    @Override
    public Page<CommentPageItemVO> pageByAnimeId(Long animeId, long pageNum, long pageSize) {
        Page<BizComment> page = this.page(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BizComment>()
                        .eq(BizComment::getAnimeId, animeId)
                        .eq(BizComment::getDelFlag, 0)
                        .eq(BizComment::getParentId, 0) // 仅主评论
                        .orderByDesc(BizComment::getCreateTime)
        );

        List<CommentPageItemVO> voList = buildCommentVOs(page.getRecords(), true);

        Page<CommentPageItemVO> result = new Page<>(pageNum, pageSize, page.getTotal());
        result.setPages(page.getPages());
        result.setRecords(voList);
        return result;
    }

    @Override
    public Page<CommentPageItemVO> pageAll(long pageNum, long pageSize, String keyword) {
        LambdaQueryWrapper<BizComment> wrapper = new LambdaQueryWrapper<BizComment>()
                .eq(BizComment::getDelFlag, 0)
                .orderByDesc(BizComment::getCreateTime);

        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(BizComment::getContent, keyword);
        }

        Page<BizComment> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<CommentPageItemVO> voList = buildCommentVOs(page.getRecords(), false);
        Page<CommentPageItemVO> result = new Page<>(pageNum, pageSize, page.getTotal());
        result.setPages(page.getPages());
        result.setRecords(voList);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long id) {
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizComment replyComment(CommentReplyRequestDTO request) {
        // 校验父评论存在
        BizComment parent = this.getById(request.getCommentId());
        if (parent == null) {
            throw new RuntimeException("评论不存在");
        }
        BizComment reply = new BizComment();
        reply.setAnimeId(parent.getAnimeId());
        reply.setUserId(request.getUserId());
        reply.setContent(request.getContent());
        reply.setParentId(parent.getId()); // 关联到父评论
        reply.setReplyToUserId(request.getReplyToUserId());
        reply.setReplyToNickname(request.getReplyToNickname());
        reply.setLikes(0);
        this.save(reply);
        log.info("评论回复成功, replyId: {}, parentId: {}", reply.getId(), parent.getId());
        return reply;
    }

    @Override
    public List<CommentPageItemVO> getReplies(Long commentId) {
        List<BizComment> replies = this.list(
                new LambdaQueryWrapper<BizComment>()
                        .eq(BizComment::getParentId, commentId)
                        .eq(BizComment::getDelFlag, 0)
                        .orderByAsc(BizComment::getCreateTime)
        );
        return buildCommentVOs(replies, false);
    }

    @Override
    public Page<CommentPageItemVO> pageByUserId(Long userId, long pageNum, long pageSize) {
        Page<BizComment> page = this.page(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BizComment>()
                        .eq(BizComment::getUserId, userId)
                        .eq(BizComment::getDelFlag, 0)
                        .orderByDesc(BizComment::getCreateTime)
        );
        List<CommentPageItemVO> voList = buildCommentVOs(page.getRecords(), false);
        Page<CommentPageItemVO> result = new Page<>(pageNum, pageSize, page.getTotal());
        result.setPages(page.getPages());
        result.setRecords(voList);
        return result;
    }

    // ========== 私有方法 ==========

    private List<CommentPageItemVO> buildCommentVOs(List<BizComment> comments, boolean withReplies) {
        if (comments.isEmpty()) return Collections.emptyList();

        Set<Long> userIds = comments.stream().map(BizComment::getUserId).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = userIds.isEmpty() ? Collections.emptyMap()
                : sysUserService.listByIds(userIds).stream().collect(Collectors.toMap(SysUser::getId, u -> u));

        return comments.stream().map(comment -> {
            CommentPageItemVO vo = toVO(comment, userMap);
            if (withReplies && comment.getParentId() == 0) {
                // 附带前3条回复预览
                List<BizComment> replyList = this.list(
                        new LambdaQueryWrapper<BizComment>()
                                .eq(BizComment::getParentId, comment.getId())
                                .eq(BizComment::getDelFlag, 0)
                                .orderByAsc(BizComment::getCreateTime)
                                .last("LIMIT 3")
                );
                Set<Long> replyUserIds = replyList.stream().map(BizComment::getUserId).collect(Collectors.toSet());
                Map<Long, SysUser> replyUserMap = replyUserIds.isEmpty() ? Collections.emptyMap()
                        : sysUserService.listByIds(replyUserIds).stream().collect(Collectors.toMap(SysUser::getId, u -> u));
                vo.setReplies(replyList.stream().map(r -> toVO(r, replyUserMap)).toList());
                // 回复总数
                long totalReplies = this.count(new LambdaQueryWrapper<BizComment>()
                        .eq(BizComment::getParentId, comment.getId())
                        .eq(BizComment::getDelFlag, 0));
                vo.setReplyCount((int) totalReplies);
            }
            return vo;
        }).toList();
    }

    private CommentPageItemVO toVO(BizComment comment, Map<Long, SysUser> userMap) {
        CommentPageItemVO vo = new CommentPageItemVO();
        vo.setId(comment.getId());
        vo.setAnimeId(comment.getAnimeId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setParentId(comment.getParentId());
        vo.setReplyToUserId(comment.getReplyToUserId());
        vo.setReplyToNickname(comment.getReplyToNickname());
        vo.setLikes(comment.getLikes());
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
     * 发送番剧评论回复提示私信
     * @param parentCommentId 父评论ID
     * @param commentUserId 评论用户ID
     * @param content 评论内容
     */
    private void sendAnimeCommentReplyNotification(Long parentCommentId, Long commentUserId, String content) {
        try {
            // 获取父评论
            BizComment parentComment = this.getById(parentCommentId);
            if (parentComment == null) return;
            
            Long replyToUserId = parentComment.getUserId();
            if (replyToUserId == null || replyToUserId.equals(commentUserId)) {
                return; // 回复自己不发送通知
            }
            
            SysUser commentUser = sysUserService.getById(commentUserId);
            if (commentUser == null) return;
            
            String commentUserName = commentUser.getNickname() != null ? commentUser.getNickname() : commentUser.getUsername();
            
            String messageContent = String.format("%s 回复了你的番剧评论：%s", commentUserName, content.length() > 50 ? content.substring(0, 50) + "..." : content);
            
            MessageSendDTO dto = new MessageSendDTO();
            dto.setToUserId(replyToUserId);
            dto.setContent(messageContent);
            messageService.sendMessage(dto);
            log.info("番剧评论回复提示私信已发送，fromUserId: {}, toUserId: {}", commentUserId, replyToUserId);
            
        } catch (Exception e) {
            log.warn("发送番剧评论回复提示私信失败", e);
        }
    }
}
