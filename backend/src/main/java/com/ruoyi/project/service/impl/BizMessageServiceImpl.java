package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.dto.MessageSendDTO;
import com.ruoyi.project.domain.entity.BizMessage;
import com.ruoyi.project.domain.entity.SysUser;
import com.ruoyi.project.domain.vo.ConversationVO;
import com.ruoyi.project.domain.vo.MessageVO;
import com.ruoyi.project.mapper.BizMessageMapper;
import com.ruoyi.project.mapper.SysUserMapper;
import com.ruoyi.project.service.IBizMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BizMessageServiceImpl implements IBizMessageService {

    private final BizMessageMapper messageMapper;
    private final SysUserMapper userMapper;
    private final BizUserPointsLogServiceImpl pointsLogService;

    @Override
    @Transactional
    public void sendMessage(MessageSendDTO dto) {
        Long fromUserId = SecurityUtils.getUserId();
        if (fromUserId.equals(dto.getToUserId())) {
            throw new RuntimeException("不能给自己发私信");
        }

        BizMessage message = new BizMessage();
        message.setFromUserId(fromUserId);
        message.setToUserId(dto.getToUserId());
        message.setContent(dto.getContent());
        message.setIsRead(false);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
    }

    @Override
    public List<MessageVO> getConversation(Long userId, Long otherUserId, int page, int size) {
        Page<BizMessage> messagePage = new Page<>(page, size);
        LambdaQueryWrapper<BizMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
            .eq(BizMessage::getFromUserId, userId).eq(BizMessage::getToUserId, otherUserId)
            .or()
            .eq(BizMessage::getFromUserId, otherUserId).eq(BizMessage::getToUserId, userId)
        );
        wrapper.orderByDesc(BizMessage::getCreateTime);
        Page<BizMessage> result = messageMapper.selectPage(messagePage, wrapper);

        List<BizMessage> messages = result.getRecords();
        if (messages.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> userIds = new HashSet<>();
        messages.forEach(m -> {
            if (m.getFromUserId() != null) userIds.add(m.getFromUserId());
            if (m.getToUserId() != null) userIds.add(m.getToUserId());
        });

        Map<Long, SysUser> userMap = userIds.isEmpty() ? Map.of() :
            userMapper.selectBatchIds(userIds).stream()
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        return messages.stream().map(m -> {
            MessageVO vo = new MessageVO();
            BeanUtils.copyProperties(m, vo);
            SysUser fromUser = userMap.get(m.getFromUserId());
            SysUser toUser = userMap.get(m.getToUserId());
            if (fromUser != null) {
                vo.setFromUsername(fromUser.getUsername());
                vo.setFromNickname(fromUser.getNickname());
                vo.setFromAvatar(fromUser.getAvatar());
            }
            if (toUser != null) {
                vo.setToUsername(toUser.getUsername());
                vo.setToNickname(toUser.getNickname());
                vo.setToAvatar(toUser.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ConversationVO> getConversationList(Long userId) {
        LambdaQueryWrapper<BizMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(BizMessage::getFromUserId, userId).or().eq(BizMessage::getToUserId, userId));
        wrapper.orderByDesc(BizMessage::getCreateTime);
        List<BizMessage> messages = messageMapper.selectList(wrapper);

        Map<Long, List<BizMessage>> grouped = messages.stream()
            .collect(Collectors.groupingBy(m -> {
                if (m.getFromUserId() != null && m.getFromUserId().equals(userId)) return m.getToUserId();
                if (m.getToUserId() != null && m.getToUserId().equals(userId)) return m.getFromUserId();
                return null;
            }));

        grouped.remove(null);

        List<ConversationVO> conversations = new ArrayList<>();
        Set<Long> userIds = grouped.keySet();
        Map<Long, SysUser> userMap = userIds.isEmpty() ? Map.of() :
            userMapper.selectBatchIds(userIds).stream()
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        for (Map.Entry<Long, List<BizMessage>> entry : grouped.entrySet()) {
            Long otherUserId = entry.getKey();
            List<BizMessage> convMessages = entry.getValue();

            ConversationVO vo = new ConversationVO();
            vo.setUserId(otherUserId);

            SysUser otherUser = userMap.get(otherUserId);
            if (otherUser != null) {
                vo.setUsername(otherUser.getUsername());
                vo.setNickname(otherUser.getNickname());
                vo.setAvatar(otherUser.getAvatar());
            }

            if (!convMessages.isEmpty()) {
                BizMessage lastMessage = convMessages.get(0);
                if (lastMessage != null) {
                    vo.setLastMessage(lastMessage.getContent());
                    vo.setLastMessageTime(lastMessage.getCreateTime());
                }
            }

            long unreadCount = convMessages.stream()
                .filter(m -> m.getToUserId().equals(userId) && !m.getIsRead())
                .count();
            vo.setUnreadCount((int) unreadCount);

            conversations.add(vo);
        }

        conversations.sort((a, b) -> {
            LocalDateTime aTime = a.getLastMessageTime();
            LocalDateTime bTime = b.getLastMessageTime();
            if (aTime == null && bTime == null) return 0;
            if (aTime == null) return 1;
            if (bTime == null) return -1;
            return bTime.compareTo(aTime);
        });

        return conversations;
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long otherUserId) {
        LambdaUpdateWrapper<BizMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(BizMessage::getToUserId, userId)
            .eq(BizMessage::getFromUserId, otherUserId)
            .eq(BizMessage::getIsRead, false)
            .set(BizMessage::getIsRead, true);
        messageMapper.update(null, wrapper);
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        LambdaQueryWrapper<BizMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizMessage::getToUserId, userId)
            .eq(BizMessage::getIsRead, false);
        return Math.toIntExact(messageMapper.selectCount(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean claimRegistrationBonus(Long userId) {
        return pointsLogService.awardRegistrationBonus(userId);
    }
}
