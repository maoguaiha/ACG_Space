package com.ruoyi.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.domain.dto.ArticleReviewRequestDTO;
import com.ruoyi.project.domain.entity.BizArticle;
import com.ruoyi.project.domain.vo.ArticleListVO;
import com.ruoyi.project.service.IBizArticleService;
import com.ruoyi.project.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/article/admin")
@RequiredArgsConstructor
public class AdminArticleController {

    private final IBizArticleService articleService;
    private final ISysUserService sysUserService;

    @GetMapping("/reviewList")
    public Result<Page<ArticleListVO>> reviewList(@RequestParam(defaultValue = "1") long page, @RequestParam(defaultValue = "10") long size) {
        LambdaQueryWrapper<BizArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizArticle::getDelFlag, 0).eq(BizArticle::getStatus, 3).orderByDesc(BizArticle::getCreateTime);
        Page<BizArticle> bizPage = articleService.page(new Page<>(page, size), wrapper);

        Set<Long> authorIds = bizPage.getRecords().stream().map(BizArticle::getAuthorId).collect(Collectors.toSet());
        Map<Long, com.ruoyi.project.domain.entity.SysUser> userMap = authorIds.isEmpty() ? Map.of() :
                sysUserService.listByIds(authorIds).stream().collect(Collectors.toMap(com.ruoyi.project.domain.entity.SysUser::getId, u -> u));

        Page<ArticleListVO> result = new Page<>(page, size, bizPage.getTotal());
        result.setPages(bizPage.getPages());
        result.setRecords(bizPage.getRecords().stream().map(article -> {
            ArticleListVO vo = new ArticleListVO();
            vo.setId(article.getId());
            vo.setTitle(article.getTitle());
            vo.setSummary(article.getSummary());
            vo.setCoverUrl(article.getCoverUrl());
            vo.setAuthorId(article.getAuthorId());
            vo.setCategory(article.getCategory());
            vo.setTags(article.getTags());
            vo.setViewCount(article.getViewCount());
            vo.setLikeCount(article.getLikeCount());
            vo.setDislikeCount(article.getDislikeCount());
            vo.setCommentCount(article.getCommentCount());
            vo.setStatus(article.getStatus());
            vo.setIsVipOnly(article.getIsVipOnly());
            vo.setIsFeatured(article.getIsFeatured());
            vo.setCreateTime(article.getCreateTime());
            com.ruoyi.project.domain.entity.SysUser author = userMap.get(article.getAuthorId());
            if (author != null) {
                vo.setAuthorNickname(author.getNickname());
                vo.setAuthorAvatar(author.getAvatar());
            }
            return vo;
        }).toList());

        return Result.success(result);
    }

    @PutMapping("/review")
    public Result<Void> review(@RequestBody ArticleReviewRequestDTO req) {
        BizArticle article = articleService.getById(req.getId());
        if (article == null) return Result.error("文章不存在");
        if (req.getApprove() == null) return Result.error("approve 不能为空");
        if (req.getApprove()) {
            article.setStatus(1);
            article.setRejectReason(null);
        } else {
            article.setStatus(4);
            article.setRejectReason(req.getRejectReason());
        }
        articleService.updateArticle(article);
        return Result.success();
    }
}
