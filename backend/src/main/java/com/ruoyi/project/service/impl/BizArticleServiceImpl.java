package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.entity.BizArticle;
import com.ruoyi.project.domain.entity.BizArticleReaction;
import com.ruoyi.project.domain.entity.SysUser;
import com.ruoyi.project.domain.vo.ArticleDetailVO;
import com.ruoyi.project.domain.vo.ArticleListVO;
import com.ruoyi.project.mapper.BizArticleMapper;
import com.ruoyi.project.service.IBizArticleReactionService;
import com.ruoyi.project.service.IBizArticleService;
import com.ruoyi.project.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizArticleServiceImpl extends ServiceImpl<BizArticleMapper, BizArticle> implements IBizArticleService {

    private final ISysUserService sysUserService;

    @Autowired @Lazy
    private IBizArticleReactionService reactionService;

    @Override
    public Page<ArticleListVO> pageArticleList(long pageNum, long pageSize, String keyword, String category, Integer status, String sortBy) {
        LambdaQueryWrapper<BizArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizArticle::getDelFlag, 0);

        if (status != null) {
            wrapper.eq(BizArticle::getStatus, status);
        }

        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(BizArticle::getTitle, keyword).or().like(BizArticle::getSummary, keyword));
        }
        if (category != null && !category.isBlank()) {
            wrapper.eq(BizArticle::getCategory, category);
        }

        if ("views".equalsIgnoreCase(sortBy)) {
            wrapper.orderByDesc(BizArticle::getViewCount).orderByDesc(BizArticle::getCreateTime);
        } else if ("likes".equalsIgnoreCase(sortBy)) {
            wrapper.orderByDesc(BizArticle::getLikeCount).orderByDesc(BizArticle::getCreateTime);
        } else {
            wrapper.orderByDesc(BizArticle::getIsFeatured).orderByDesc(BizArticle::getCreateTime);
        }

        Page<BizArticle> page = this.page(new Page<>(pageNum, pageSize), wrapper);

        Set<Long> authorIds = page.getRecords().stream()
                .map(BizArticle::getAuthorId)
                .collect(Collectors.toSet());
        Map<Long, SysUser> userMap = authorIds.isEmpty() ? Collections.emptyMap() :
                sysUserService.listByIds(authorIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, u -> u));

        Page<ArticleListVO> result = new Page<>(pageNum, pageSize, page.getTotal());
        result.setPages(page.getPages());
        result.setRecords(page.getRecords().stream().map(article -> {
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
            SysUser author = userMap.get(article.getAuthorId());
            if (author != null) {
                vo.setAuthorNickname(author.getNickname());
                vo.setAuthorAvatar(author.getAvatar());
            }
            return vo;
        }).toList());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleDetailVO getArticleDetail(Long id) {
        BizArticle article = this.getById(id);
        if (article == null) {
            return null;
        }

        ArticleDetailVO vo = new ArticleDetailVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setContent(article.getContent());
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
        vo.setUpdateTime(article.getUpdateTime());

        SysUser author = sysUserService.getById(article.getAuthorId());
        if (author != null) {
            vo.setAuthorNickname(author.getNickname());
            vo.setAuthorAvatar(author.getAvatar());
        }

        Long userId = SecurityUtils.getUserId();
        if (userId != null) {
            LambdaQueryWrapper<BizArticleReaction> reactionWrapper = new LambdaQueryWrapper<>();
            reactionWrapper.eq(BizArticleReaction::getArticleId, id)
                    .eq(BizArticleReaction::getUserId, userId);
            BizArticleReaction reaction = reactionService.getOne(reactionWrapper);
            if (reaction != null) {
                vo.setUserReaction(reaction.getReactionType());
            }
        }

        incrementViewCount(id);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizArticle createArticle(BizArticle article) {
        if (article.getAuthorId() == null) {
            Long userId = SecurityUtils.getUserId();
            article.setAuthorId(userId != null ? userId : 1L);
        }
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setDislikeCount(0);
        article.setCommentCount(0);
        if (article.getStatus() == null) {
            article.setStatus(0);
        }
        this.save(article);
        log.info("文章创建成功, id: {}", article.getId());
        return article;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizArticle updateArticle(BizArticle article) {
        BizArticle existing = this.getById(article.getId());
        if (existing == null) {
            throw new RuntimeException("文章不存在");
        }
        this.updateById(article);
        log.info("文章更新成功, id: {}", article.getId());
        return article;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteArticle(Long id) {
        boolean removed = this.removeById(id);
        if (!removed) {
            log.warn("文章删除失败, id: {}", id);
        }
        return removed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long id) {
        BizArticle article = this.getById(id);
        if (article != null) {
            article.setViewCount(article.getViewCount() == null ? 1 : article.getViewCount() + 1);
            this.updateById(article);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFeatured(Long id) {
        BizArticle article = this.getById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }
        article.setIsFeatured(article.getIsFeatured() != null && article.getIsFeatured() == 1 ? 0 : 1);
        return this.updateById(article);
    }

    @Override
    public List<String> getAllCategories() {
        return baseMapper.selectAllCategories();
    }
}
