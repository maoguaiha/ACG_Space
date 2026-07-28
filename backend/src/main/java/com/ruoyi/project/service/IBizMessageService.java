package com.ruoyi.project.service;

import com.ruoyi.project.domain.dto.MessageSendDTO;
import com.ruoyi.project.domain.vo.ConversationVO;
import com.ruoyi.project.domain.vo.MessageVO;
import java.util.List;

public interface IBizMessageService {

    void sendMessage(MessageSendDTO dto);

    List<MessageVO> getConversation(Long userId, Long otherUserId, int page, int size);

    List<ConversationVO> getConversationList(Long userId);

    void markAsRead(Long userId, Long otherUserId);

    Integer getUnreadCount(Long userId);

    /**
     * 领取注册积分奖励
     * @param userId 用户ID
     * @return 是否领取成功
     */
    boolean claimRegistrationBonus(Long userId);

    /**
     * 发送系统通知消息（fromUserId=1 系统管理员）
     * @param toUserId 接收者用户ID
     * @param content 消息内容
     */
    void sendSystemNotification(Long toUserId, String content);
}
