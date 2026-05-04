package com.ruoyi.project.controller;

import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.dto.MessageSendDTO;
import com.ruoyi.project.domain.vo.ConversationVO;
import com.ruoyi.project.domain.vo.MessageVO;
import com.ruoyi.project.service.IBizMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class BizMessageController {

    private final IBizMessageService messageService;

    @PostMapping("/send")
    public Result<?> sendMessage(@Validated @RequestBody MessageSendDTO dto) {
        try {
            messageService.sendMessage(dto);
            return Result.success("发送成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/conversation/{userId}")
    public Result<List<MessageVO>> getConversation(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        Long currentUserId = SecurityUtils.getUserId();
        List<MessageVO> messages = messageService.getConversation(currentUserId, userId, page, size);
        return Result.success(messages);
    }

    @GetMapping("/list")
    public Result<List<ConversationVO>> getConversationList() {
        Long userId = SecurityUtils.getUserId();
        List<ConversationVO> list = messageService.getConversationList(userId);
        return Result.success(list);
    }

    @PutMapping("/read/{userId}")
    public Result<?> markAsRead(@PathVariable Long userId) {
        Long currentUserId = SecurityUtils.getUserId();
        messageService.markAsRead(currentUserId, userId);
        return Result.success("已标记已读");
    }

    @GetMapping("/unread")
    public Result<Integer> getUnreadCount() {
        Long userId = SecurityUtils.getUserId();
        Integer count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }
}
