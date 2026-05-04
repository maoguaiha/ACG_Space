package com.ruoyi.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.project.domain.entity.BizArticle;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文章 Mapper 接口
 */
public interface BizArticleMapper extends BaseMapper<BizArticle> {

    /**
     * 获取所有文章分类列表
     */
    @Select("SELECT DISTINCT category FROM biz_article WHERE category IS NOT NULL AND category != '' AND del_flag = 0 ORDER BY category")
    List<String> selectAllCategories();
}