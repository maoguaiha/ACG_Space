package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizArticle;
import com.ruoyi.project.domain.vo.ArticleDetailVO;
import com.ruoyi.project.domain.vo.ArticleListVO;

import java.util.List;

public interface IBizArticleService extends IService<BizArticle> {

    Page<ArticleListVO> pageArticleList(long pageNum, long pageSize, String keyword, String category, Integer status, String sortBy);

    ArticleDetailVO getArticleDetail(Long id);

    BizArticle createArticle(BizArticle article);

    BizArticle updateArticle(BizArticle article);

    boolean deleteArticle(Long id);

    void incrementViewCount(Long id);

    boolean toggleFeatured(Long id);

    /**
     * 获取所有文章分类列表
     */
    List<String> getAllCategories();
}
