package com.ruoyi.project.controller;

import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.dto.MessageSendDTO;
import com.ruoyi.project.domain.vo.ConversationVO;
import com.ruoyi.project.domain.vo.MessageVO;
import com.ruoyi.project.service.IBizMessageService;
import com.ruoyi.project.service.IBizUserPointsLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class BizMessageController {

    private final IBizMessageService messageService;
    private final IBizUserPointsLogService pointsLogService;

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

    @PostMapping("/claim-bonus")
    public Result<?> claimRegistrationBonus() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        try {
            boolean success = messageService.claimRegistrationBonus(userId);
            int currentPoints = pointsLogService.getUserPoints(userId);
            Map<String, Object> result = new HashMap<>();
            if (success) {
                result.put("message", "领取成功，已获得2600积分");
            } else {
                result.put("message", "您已领取过积分，无需重复领取");
            }
            result.put("points", currentPoints);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
