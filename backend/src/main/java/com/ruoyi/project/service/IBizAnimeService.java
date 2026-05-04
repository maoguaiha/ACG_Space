package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizAnime;

public interface IBizAnimeService extends IService<BizAnime> {
    
    /**
     * 通过 Bangumi API 导入或同步番剧数据
     * @param bgmId 番组计划的条目ID
     * @return 导入或更新后的番剧实体
     */
    BizAnime syncFromBangumi(Integer bgmId);

    /**
     * 获取每日放送表 (新番时间表)
     * @return 每日放送数据
     */
    com.alibaba.fastjson2.JSONArray getBangumiCalendar();
    /**
     * 在 Bangumi 中搜索番剧
     * @param keywords 关键词
     * @return 搜索结果列表
     */
    com.alibaba.fastjson2.JSONObject searchBangumi(String keywords);

    /**
     * 批量从 Bangumi 导入番剧（若已存在则更新）
     * @param bgmIds Bangumi 条目 ID 列表
     * @return 导入或更新后的番剧列表
     */
    java.util.List<BizAnime> importFromBangumi(java.util.List<Integer> bgmIds);
}
