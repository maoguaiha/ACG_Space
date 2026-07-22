package com.ruoyi.project.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.domain.entity.BizAnime;
import com.ruoyi.project.integration.BangumiApiClient;
import com.ruoyi.project.mapper.BizAnimeMapper;
import com.ruoyi.project.service.IBizAnimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class BizAnimeServiceImpl extends ServiceImpl<BizAnimeMapper, BizAnime> implements IBizAnimeService {

    private final BangumiApiClient bangumiApiClient;
    private final org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    @Override
    public com.alibaba.fastjson2.JSONArray getBangumiCalendar() {
        String cacheKey = "bangumi:calendar";
        String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null && !cachedData.isEmpty()) {
            return com.alibaba.fastjson2.JSON.parseArray(cachedData);
        }
        
        com.alibaba.fastjson2.JSONArray calendar = bangumiApiClient.getCalendar();
        if (calendar != null && !calendar.isEmpty()) {
            stringRedisTemplate.opsForValue().set(cacheKey, calendar.toJSONString(), 1, java.util.concurrent.TimeUnit.HOURS);
        }
        return calendar;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizAnime syncFromBangumi(Integer bgmId) {
        log.info("开始同步 Bangumi 数据, bgmId: {}", bgmId);
        
        // 1. 调用第三方 API 获取数据
        JSONObject bgmData = bangumiApiClient.getSubjectDetails(bgmId);
        if (bgmData == null) {
            throw new RuntimeException("无法从 Bangumi 获取数据: " + bgmId);
        }

        // 2. 检查数据库中是否已存在该番剧
        BizAnime anime = this.getOne(new LambdaQueryWrapper<BizAnime>().eq(BizAnime::getBgmId, bgmId));
        if (anime == null) {
            anime = new BizAnime();
            anime.setBgmId(bgmId);
            anime.setDelFlag(0);
            anime.setCreateTime(java.time.LocalDateTime.now());
        }
        anime.setUpdateTime(java.time.LocalDateTime.now());

        // 3. 数据映射与填充
        anime.setTitle(bgmData.getString("name_cn"));
        if (anime.getTitle() == null || anime.getTitle().isEmpty()) {
            anime.setTitle(bgmData.getString("name"));
        }
        anime.setTitleOriginal(bgmData.getString("name"));
        anime.setSummary(bgmData.getString("summary"));
        anime.setTotalEpisodes(bgmData.getInteger("eps"));
        
        // 解析开播年份 (例如从 date "2023-09-29" 中提取)
        String dateStr = bgmData.getString("date");
        if (dateStr != null && dateStr.length() >= 4) {
            anime.setPublishYear(Integer.parseInt(dateStr.substring(0, 4)));
        }
        anime.setStatus(resolveAnimeStatus(bgmData));

        // 提取海报图
        JSONObject images = bgmData.getJSONObject("images");
        if (images != null) {
            anime.setCoverUrl(images.getString("large"));
        }

        // 提取评分
        JSONObject ratingObj = bgmData.getJSONObject("rating");
        if (ratingObj != null && ratingObj.getBigDecimal("score") != null) {
            anime.setRating(ratingObj.getBigDecimal("score"));
        }

        // 提取类型标签（从 tags 字段）
        com.alibaba.fastjson2.JSONArray tags = bgmData.getJSONArray("tags");
        if (tags != null && !tags.isEmpty()) {
            StringBuilder genreBuilder = new StringBuilder();
            for (int i = 0; i < tags.size(); i++) {
                JSONObject tag = tags.getJSONObject(i);
                if (tag != null && tag.getString("name") != null) {
                    if (i > 0) {
                        genreBuilder.append(",");
                    }
                    genreBuilder.append(tag.getString("name"));
                }
            }
            anime.setGenre(genreBuilder.toString());
        }

        // 4. 保存或更新数据库
        try {
            this.saveOrUpdate(anime);
            log.info("Bangumi 数据同步完成, 本地番剧ID: {}", anime.getId());
        } catch (org.springframework.dao.DuplicateKeyException dke) {
            // 可能有并发插入导致唯一键冲突（另一个线程/请求已插入相同 bgmId）
            log.warn("检测到 bgmId 唯一键冲突，尝试回退并更新已存在记录: {}", bgmId);
            BizAnime existing = this.getOne(new LambdaQueryWrapper<BizAnime>().eq(BizAnime::getBgmId, bgmId));
            if (existing != null) {
                // 合并字段并更新
                anime.setId(existing.getId());
                this.updateById(anime);
                log.info("冲突处理完成，已更新本地番剧ID: {}", anime.getId());
            } else {
                // 若仍然找不到，抛出原始异常以便上层记录
                throw dke;
            }
        }
        
        return anime;
    }
    @Override
    public com.alibaba.fastjson2.JSONObject searchBangumi(String keywords) {
        return bangumiApiClient.searchSubject(keywords);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public java.util.List<BizAnime> importFromBangumi(java.util.List<Integer> bgmIds) {
        java.util.List<BizAnime> imported = new java.util.ArrayList<>();
        if (bgmIds == null || bgmIds.isEmpty()) return imported;

        for (Integer id : bgmIds) {
            try {
                BizAnime a = this.syncFromBangumi(id);
                if (a != null) imported.add(a);
            } catch (Exception ex) {
                log.warn("导入 Bangumi ID {} 失败: {}", id, ex.getMessage());
            }
        }
        return imported;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateGenresFromBangumi() {
        log.info("开始批量更新所有番剧的类型信息");
        
        // 获取所有已同步的番剧（有 bgmId 的）
        List<BizAnime> allAnime = this.list(new LambdaQueryWrapper<BizAnime>()
                .eq(BizAnime::getDelFlag, 0)
                .isNotNull(BizAnime::getBgmId));
        
        int updatedCount = 0;
        int failedCount = 0;
        
        for (BizAnime anime : allAnime) {
            Integer bgmId = anime.getBgmId();
            if (bgmId == null) continue;
            
            try {
                JSONObject bgmData = bangumiApiClient.getSubjectDetails(bgmId);
                if (bgmData != null) {
                    // 提取类型标签
                    com.alibaba.fastjson2.JSONArray tags = bgmData.getJSONArray("tags");
                    if (tags != null && !tags.isEmpty()) {
                        StringBuilder genreBuilder = new StringBuilder();
                        for (int i = 0; i < tags.size(); i++) {
                            JSONObject tag = tags.getJSONObject(i);
                            if (tag != null && tag.getString("name") != null) {
                                if (i > 0) {
                                    genreBuilder.append(",");
                                }
                                genreBuilder.append(tag.getString("name"));
                            }
                        }
                        String newGenre = genreBuilder.toString();
                        if (!newGenre.equals(anime.getGenre())) {
                            anime.setGenre(newGenre);
                            this.updateById(anime);
                            updatedCount++;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("更新番剧 {} (bgmId: {}) 的类型信息失败: {}", anime.getTitle(), bgmId, e.getMessage());
                failedCount++;
            }
        }
        
        log.info("批量更新类型信息完成，成功: {}, 失败: {}", updatedCount, failedCount);
        return updatedCount;
    }

    /**
     * 将 Bangumi 字段映射为本地状态：
     * 0 连载中，1 已完结，2 未开播
     */
    private Integer resolveAnimeStatus(JSONObject bgmData) {
        String dateStr = bgmData.getString("date");
        Integer airedEpisodes = bgmData.getInteger("eps");
        Integer totalEpisodes = bgmData.getInteger("total_episodes");

        // 开播日期在未来：未开播
        if (dateStr != null && !dateStr.isBlank()) {
            try {
                LocalDate airDate = LocalDate.parse(dateStr);
                if (airDate.isAfter(LocalDate.now())) {
                    return 2;
                }
            } catch (DateTimeParseException ignored) {
                // 日期格式不标准时忽略日期判定，走后续兜底逻辑
            }
        }

        // 已放送集数 >= 总集数：已完结
        if (airedEpisodes != null && totalEpisodes != null && totalEpisodes > 0 && airedEpisodes >= totalEpisodes) {
            return 1;
        }

        // 其余情况默认连载中
        return 0;
    }

    @Override
    @Cacheable(value = "anime:featured", key = "'list'", unless = "#result == null || #result.isEmpty()")
    public List<BizAnime> getFeaturedAnime() {
        return this.list(new LambdaQueryWrapper<BizAnime>()
                .eq(BizAnime::getFeatured, 1)
                .orderByDesc(BizAnime::getCreateTime));
    }

    @Override
    @Cacheable(value = "anime:list", key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<BizAnime> getAllAnimeList() {
        return this.list(new LambdaQueryWrapper<BizAnime>()
                .orderByDesc(BizAnime::getFeatured)
                .orderByDesc(BizAnime::getCreateTime));
    }

    @Override
    @Cacheable(value = "anime:page", key = "'p' + #page + ':s' + #size", unless = "#result == null || #result.getRecords() == null || #result.getRecords().isEmpty()")
    public Page<BizAnime> getAnimePage(int page, int size) {
        return this.page(new Page<>(page, size), new LambdaQueryWrapper<BizAnime>()
                .orderByDesc(BizAnime::getFeatured)
                .orderByDesc(BizAnime::getCreateTime));
    }
}
