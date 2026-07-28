package com.ruoyi.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.entity.BizAnime;
import com.ruoyi.project.domain.entity.BizArticle;
import com.ruoyi.project.domain.entity.BizComment;
import com.ruoyi.project.domain.entity.BizArticleComment;
import com.ruoyi.project.domain.entity.BizCommentReaction;
import com.ruoyi.project.domain.entity.BizArticleCommentReaction;
import com.ruoyi.project.domain.entity.SysUser;
import com.ruoyi.project.domain.vo.UserProfileVO;
import com.ruoyi.project.domain.vo.UserCommentVO;
import com.ruoyi.project.domain.vo.UserLikeHistoryVO;
import com.ruoyi.project.service.IBizAnimeFollowService;
import com.ruoyi.project.service.IBizArticleService;
import com.ruoyi.project.service.IBizCommentService;
import com.ruoyi.project.service.IBizArticleCommentService;
import com.ruoyi.project.service.IBizAnimeService;
import com.ruoyi.project.service.IBizUserFollowService;
import com.ruoyi.project.service.ISysUserService;
import com.ruoyi.project.service.impl.BizCommentReactionServiceImpl;
import com.ruoyi.project.service.impl.BizArticleCommentReactionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户主页与社区相关 API
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final ISysUserService sysUserService;
    private final IBizUserFollowService followService;
    private final IBizArticleService articleService;
    private final IBizAnimeFollowService animeFollowService;
    private final IBizCommentService commentService;
    private final IBizArticleCommentService articleCommentService;
    private final IBizAnimeService animeService;
    private final BizCommentReactionServiceImpl commentReactionService;
    private final BizArticleCommentReactionServiceImpl articleCommentReactionService;

    /**
     * 获取用户公开资料
     */
    @GetMapping("/{id}/profile")
    public Result<UserProfileVO> getProfile(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Long currentUserId = SecurityUtils.getUserId();

        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setBio(user.getBio());
        vo.setPoints(user.getPoints());
        vo.setFollowerCount(user.getFollowerCount() != null ? user.getFollowerCount() : 0);
        vo.setFollowingCount(user.getFollowingCount() != null ? user.getFollowingCount() : 0);
        vo.setVipStatus(user.getVipStatus() != null ? user.getVipStatus() : 0);
        vo.setVipExpireTime(user.getVipExpireTime());
        vo.setUserLevel(user.getUserLevel() != null ? user.getUserLevel() : 1);
        vo.setLevelExperience(user.getLevelExperience() != null ? user.getLevelExperience() : 0);

        if (currentUserId != null) {
            vo.setIsSelf(currentUserId.equals(id));
            vo.setIsFollowed(!currentUserId.equals(id) && followService.isFollowing(currentUserId, id));
        } else {
            vo.setIsSelf(false);
            vo.setIsFollowed(false);
        }

        return Result.success(vo);
    }

    /**
     * 当前用户编辑自己的资料
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody SysUser update) {
        try {
            Long userId = SecurityUtils.getUserId();
            if (userId == null) {
                return Result.error("请先登录");
            }
            SysUser user = sysUserService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            // 只允许修改这些字段
            if (update.getNickname() != null) user.setNickname(update.getNickname());
            if (update.getAvatar() != null) user.setAvatar(update.getAvatar());
            if (update.getBio() != null) user.setBio(update.getBio());
            if (update.getEmail() != null) user.setEmail(update.getEmail());
            sysUserService.updateById(user);
            return Result.success();
        } catch (Exception e) {
            log.error("更新用户资料失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 关注/取关用户
     */
    @PostMapping("/follow/{targetUserId}")
    public Result<Boolean> toggleFollow(@PathVariable Long targetUserId) {
        boolean followed = followService.toggleFollow(targetUserId);
        return Result.success(followed);
    }

    /**
     * 获取关注状态（批量）
     */
    @GetMapping("/follow/status")
    public Result<List<Long>> getFollowStatus(@RequestParam List<Long> userIds) {
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            return Result.success(List.of());
        }
        List<Long> followedIds = followService.lambdaQuery()
                .eq(com.ruoyi.project.domain.entity.BizUserFollow::getUserId, currentUserId)
                .in(com.ruoyi.project.domain.entity.BizUserFollow::getFollowUserId, userIds)
                .list()
                .stream()
                .map(com.ruoyi.project.domain.entity.BizUserFollow::getFollowUserId)
                .toList();
        return Result.success(followedIds);
    }

    /**
     * 获取某用户的文章
     */
    @GetMapping("/{id}/articles")
    public Result<Page<BizArticle>> getUserArticles(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        Page<BizArticle> page = articleService.lambdaQuery()
                .eq(BizArticle::getAuthorId, id)
                .eq(BizArticle::getStatus, 1)
                .eq(BizArticle::getDelFlag, 0)
                .orderByDesc(BizArticle::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
        return Result.success(page);
    }

    @GetMapping("/{id}/comments")
    public Result<Page<UserCommentVO>> getUserComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        
        // 获取番剧评论
        List<BizComment> animeComments = commentService.lambdaQuery()
                .eq(BizComment::getUserId, id)
                .orderByDesc(BizComment::getCreateTime)
                .list();
        
        // 获取文章评论
        List<BizArticleComment> articleComments = articleCommentService.lambdaQuery()
                .eq(BizArticleComment::getUserId, id)
                .orderByDesc(BizArticleComment::getCreateTime)
                .list();
        
        // 合并转换
        List<UserCommentVO> allComments = new ArrayList<>();
        
        // 转换番剧评论
        List<Long> animeIds = animeComments.stream()
                .map(BizComment::getAnimeId)
                .filter(animeId -> animeId != null)
                .distinct()
                .toList();
        Map<Long, BizAnime> animeMap = animeIds.isEmpty() ? Map.of() :
                animeService.listByIds(animeIds).stream()
                        .filter(a -> a.getId() != null)
                        .collect(Collectors.toMap(BizAnime::getId, a -> a));
        
        for (BizComment c : animeComments) {
            UserCommentVO vo = new UserCommentVO();
            vo.setId(c.getId());
            vo.setContent(c.getContent());
            vo.setLikes(c.getLikes());
            vo.setCreateTime(c.getCreateTime());
            vo.setType(1);
            vo.setTargetId(c.getAnimeId());
            BizAnime anime = animeMap.get(c.getAnimeId());
            if (anime != null) {
                vo.setTargetTitle(anime.getTitle());
                vo.setTargetCover(anime.getCoverUrl());
            }
            allComments.add(vo);
        }
        
        // 转换文章评论
        List<Long> articleIds = articleComments.stream()
                .map(BizArticleComment::getArticleId)
                .filter(articleId -> articleId != null)
                .distinct()
                .toList();
        Map<Long, BizArticle> articleMap = articleIds.isEmpty() ? Map.of() :
                articleService.listByIds(articleIds).stream()
                        .filter(a -> a.getId() != null)
                        .collect(Collectors.toMap(BizArticle::getId, a -> a));
        
        for (BizArticleComment c : articleComments) {
            UserCommentVO vo = new UserCommentVO();
            vo.setId(c.getId());
            vo.setContent(c.getContent());
            vo.setLikes(c.getLikes());
            vo.setCreateTime(c.getCreateTime());
            vo.setType(2);
            vo.setTargetId(c.getArticleId());
            BizArticle article = articleMap.get(c.getArticleId());
            if (article != null) {
                vo.setTargetTitle(article.getTitle());
                vo.setTargetCover(article.getCoverUrl());
            }
            allComments.add(vo);
        }
        
        // 按时间倒序
        allComments.sort(Comparator.comparing(
                UserCommentVO::getCreateTime,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));
        
        // 分页
        int total = allComments.size();
        int start = (int) ((pageNum - 1) * pageSize);
        int end = Math.min(start + (int) pageSize, total);
        List<UserCommentVO> pageRecords = start < end ? allComments.subList(start, end) : new ArrayList<>();
        
        Page<UserCommentVO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(pageRecords);
        page.setPages((total + (int) pageSize - 1) / (int) pageSize);
        
        return Result.success(page);
    }

    /**
     * 获取某用户的追番列表
     */
    @GetMapping("/{id}/follows")
    public Result<List<BizAnime>> getUserFollows(@PathVariable Long id) {
        return Result.success(animeFollowService.getUserFollowList(id));
    }

    /**
     * 将 SysUser 转换为 UserProfileVO
     */
    private UserProfileVO convertToUserProfileVO(SysUser user, Long currentUserId) {
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setBio(user.getBio());
        vo.setPoints(user.getPoints());
        vo.setFollowerCount(user.getFollowerCount());
        vo.setFollowingCount(user.getFollowingCount());
        vo.setIsSelf(currentUserId != null && currentUserId.equals(user.getId()));
        // 检查是否已关注
        if (currentUserId != null && !currentUserId.equals(user.getId())) {
            boolean isFollowed = followService.lambdaQuery()
                    .eq(com.ruoyi.project.domain.entity.BizUserFollow::getUserId, currentUserId)
                    .eq(com.ruoyi.project.domain.entity.BizUserFollow::getFollowUserId, user.getId())
                    .count() > 0;
            vo.setIsFollowed(isFollowed);
        } else {
            vo.setIsFollowed(false);
        }
        return vo;
    }

    /**
     * 获取某用户的粉丝列表
     */
    @GetMapping("/{id}/followers")
    public Result<List<UserProfileVO>> getFollowers(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getUserId();
        // 通过 followService 查询粉丝ID列表，再查用户信息
        List<Long> followerIds = followService.lambdaQuery()
                .eq(com.ruoyi.project.domain.entity.BizUserFollow::getFollowUserId, id)
                .list()
                .stream()
                .map(com.ruoyi.project.domain.entity.BizUserFollow::getUserId)
                .toList();
        if (followerIds.isEmpty()) {
            return Result.success(List.of());
        }
        List<SysUser> users = sysUserService.listByIds(followerIds);
        List<UserProfileVO> result = users.stream()
                .map(u -> convertToUserProfileVO(u, currentUserId))
                .toList();
        return Result.success(result);
    }

    /**
     * 获取某用户的关注列表
     */
    @GetMapping("/{id}/following")
    public Result<List<UserProfileVO>> getFollowing(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getUserId();
        List<Long> followingIds = followService.lambdaQuery()
                .eq(com.ruoyi.project.domain.entity.BizUserFollow::getUserId, id)
                .list()
                .stream()
                .map(com.ruoyi.project.domain.entity.BizUserFollow::getFollowUserId)
                .toList();
        if (followingIds.isEmpty()) {
            return Result.success(List.of());
        }
        List<SysUser> users = sysUserService.listByIds(followingIds);
        List<UserProfileVO> result = users.stream()
                .map(u -> convertToUserProfileVO(u, currentUserId))
                .toList();
        return Result.success(result);
    }

    /**
     * 搜索用户（社区页用户栏用）
     */
    @GetMapping("/search")
    public Result<Page<SysUser>> searchUsers(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword) {
        Page<SysUser> page = sysUserService.lambdaQuery()
                .like(keyword != null && !keyword.isBlank(), SysUser::getNickname, keyword)
                .or(keyword != null && !keyword.isBlank(),
                        w -> w.like(SysUser::getUsername, keyword))
                .orderByDesc(SysUser::getFollowerCount)
                .page(new Page<>(pageNum, pageSize));
        // 脱敏：清除密码
        page.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(page);
    }

    /**
     * 获取某用户的点赞历史（番剧评论点赞 + 文章评论点赞）
     */
    @GetMapping("/{id}/likes")
    public Result<Page<UserLikeHistoryVO>> getUserLikes(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize) {

        List<UserLikeHistoryVO> allLikes = new ArrayList<>();

        // 获取番剧评论点赞
        List<BizCommentReaction> animeLikes = commentReactionService.lambdaQuery()
                .eq(BizCommentReaction::getUserId, id)
                .eq(BizCommentReaction::getReactionType, 1)
                .orderByDesc(BizCommentReaction::getCreateTime)
                .list();

        List<Long> animeCommentIds = animeLikes.stream()
                .map(BizCommentReaction::getCommentId)
                .toList();
        Map<Long, BizComment> animeCommentMap = animeCommentIds.isEmpty() ? Map.of() :
                commentService.listByIds(animeCommentIds).stream()
                        .filter(c -> c.getId() != null)
                        .collect(Collectors.toMap(BizComment::getId, c -> c));

        for (BizCommentReaction r : animeLikes) {
            UserLikeHistoryVO vo = new UserLikeHistoryVO();
            vo.setId(r.getId());
            vo.setType(1);
            vo.setTargetId(r.getCommentId());
            vo.setCreateTime(r.getCreateTime());
            BizComment comment = animeCommentMap.get(r.getCommentId());
            if (comment != null) {
                vo.setTargetId(comment.getAnimeId());
                BizAnime anime = animeService.getById(comment.getAnimeId());
                if (anime != null) {
                    vo.setTargetTitle(anime.getTitle());
                    vo.setTargetCover(anime.getCoverUrl());
                }
            }
            allLikes.add(vo);
        }

        // 获取文章评论点赞
        List<BizArticleCommentReaction> articleLikes = articleCommentReactionService.lambdaQuery()
                .eq(BizArticleCommentReaction::getUserId, id)
                .eq(BizArticleCommentReaction::getReactionType, 1)
                .orderByDesc(BizArticleCommentReaction::getCreateTime)
                .list();

        List<Long> articleCommentIds = articleLikes.stream()
                .map(BizArticleCommentReaction::getArticleCommentId)
                .toList();
        Map<Long, BizArticleComment> articleCommentMap = articleCommentIds.isEmpty() ? Map.of() :
                articleCommentService.listByIds(articleCommentIds).stream()
                        .filter(c -> c.getId() != null)
                        .collect(Collectors.toMap(BizArticleComment::getId, c -> c));

        for (BizArticleCommentReaction r : articleLikes) {
            UserLikeHistoryVO vo = new UserLikeHistoryVO();
            vo.setId(r.getId());
            vo.setType(2);
            vo.setTargetId(r.getArticleCommentId());
            vo.setCreateTime(r.getCreateTime());
            BizArticleComment comment = articleCommentMap.get(r.getArticleCommentId());
            if (comment != null) {
                vo.setTargetId(comment.getArticleId());
                BizArticle article = articleService.getById(comment.getArticleId());
                if (article != null) {
                    vo.setTargetTitle(article.getTitle());
                    vo.setTargetCover(article.getCoverUrl());
                }
            }
            allLikes.add(vo);
        }

        // 按时间倒序
        allLikes.sort(Comparator.comparing(
                UserLikeHistoryVO::getCreateTime,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        // 分页
        int total = allLikes.size();
        int start = (int) ((pageNum - 1) * pageSize);
        int end = Math.min(start + (int) pageSize, total);
        List<UserLikeHistoryVO> pageRecords = start < end ? allLikes.subList(start, end) : new ArrayList<>();

        Page<UserLikeHistoryVO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(pageRecords);
        page.setPages((total + (int) pageSize - 1) / (int) pageSize);

        return Result.success(page);
    }
}
