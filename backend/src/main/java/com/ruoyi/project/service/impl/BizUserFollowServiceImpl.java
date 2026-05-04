package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.dto.MessageSendDTO;
import com.ruoyi.project.domain.entity.BizUserFollow;
import com.ruoyi.project.domain.entity.SysUser;
import com.ruoyi.project.mapper.BizUserFollowMapper;
import com.ruoyi.project.service.IBizMessageService;
import com.ruoyi.project.service.IBizUserFollowService;
import com.ruoyi.project.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizUserFollowServiceImpl extends ServiceImpl<BizUserFollowMapper, BizUserFollow> implements IBizUserFollowService {

    private final ISysUserService sysUserService;
    private final IBizMessageService messageService;

    @Override
    @Transactional
    public boolean toggleFollow(Long targetUserId) {
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            throw new RuntimeException("请先登录");
        }
        if (currentUserId.equals(targetUserId)) {
            throw new RuntimeException("不能关注自己");
        }

        LambdaQueryWrapper<BizUserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizUserFollow::getUserId, currentUserId)
                .eq(BizUserFollow::getFollowUserId, targetUserId);

        BizUserFollow existing = this.getOne(wrapper);

        if (existing != null) {
            // 已关注 → 取关
            this.removeById(existing.getId());
            // 更新双方计数
            sysUserService.update().setSql("follower_count = follower_count - 1").eq("id", targetUserId).update();
            sysUserService.update().setSql("following_count = following_count - 1").eq("id", currentUserId).update();
            return false; // 已取消
        } else {
            // 未关注 → 关注
            BizUserFollow follow = new BizUserFollow();
            follow.setUserId(currentUserId);
            follow.setFollowUserId(targetUserId);
            follow.setCreateTime(LocalDateTime.now());
            this.save(follow);
            // 更新双方计数
            sysUserService.update().setSql("follower_count = follower_count + 1").eq("id", targetUserId).update();
            sysUserService.update().setSql("following_count = following_count + 1").eq("id", currentUserId).update();
            
            // 发送关注提示私信
            try {
                SysUser currentUser = sysUserService.getById(currentUserId);
                String followMessage = String.format("%s 关注了你！", currentUser.getNickname() != null ? currentUser.getNickname() : currentUser.getUsername());
                MessageSendDTO dto = new MessageSendDTO();
                dto.setToUserId(targetUserId);
                dto.setContent(followMessage);
                messageService.sendMessage(dto);
                log.info("关注提示私信已发送，fromUserId: {}, toUserId: {}", currentUserId, targetUserId);
            } catch (Exception e) {
                log.warn("发送关注提示私信失败", e);
            }
            
            return true; // 已关注
        }
    }

    @Override
    public boolean isFollowing(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null) return false;
        return this.count(new LambdaQueryWrapper<BizUserFollow>()
                .eq(BizUserFollow::getUserId, userId)
                .eq(BizUserFollow::getFollowUserId, targetUserId)) > 0;
    }

    @Override
    public long getFollowerCount(Long userId) {
        return this.count(new LambdaQueryWrapper<BizUserFollow>()
                .eq(BizUserFollow::getFollowUserId, userId));
    }

    @Override
    public long getFollowingCount(Long userId) {
        return this.count(new LambdaQueryWrapper<BizUserFollow>()
                .eq(BizUserFollow::getUserId, userId));
    }
}
