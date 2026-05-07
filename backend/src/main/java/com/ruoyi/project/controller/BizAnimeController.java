package com.ruoyi.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.domain.entity.BizAnime;
import com.ruoyi.project.service.IBizAnimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anime")
@RequiredArgsConstructor
public class BizAnimeController {

    private final IBizAnimeService animeService;

    /**
     * 获取社区番剧库列表
     */
    @GetMapping("/list")
    public Result<List<BizAnime>> list() {
        List<BizAnime> list = animeService.list(new LambdaQueryWrapper<BizAnime>()
                .orderByDesc(BizAnime::getFeatured)
                .orderByDesc(BizAnime::getCreateTime));
        return Result.success(list);
    }

    /**
     * 获取社区番剧库分页列表（前台分页）
     */
    @GetMapping("/library/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<BizAnime>> libraryPage(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer publishYear,
            @RequestParam(required = false) String genres,
            @RequestParam(defaultValue = "default") String sortBy) {
        LambdaQueryWrapper<BizAnime> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAnime::getDelFlag, 0);

        if (title != null && !title.isBlank()) {
            wrapper.and(w -> w.like(BizAnime::getTitle, title).or().like(BizAnime::getTitleOriginal, title));
        }
        if (status != null) {
            wrapper.eq(BizAnime::getStatus, status);
        }
        if (publishYear != null) {
            wrapper.eq(BizAnime::getPublishYear, publishYear);
        }
        if (genres != null && !genres.isBlank()) {
            String[] genreArray = genres.split(",");
            for (String genre : genreArray) {
                if (!genre.isBlank()) {
                    wrapper.like(BizAnime::getGenre, genre.trim());
                }
            }
        }

        if ("rating".equalsIgnoreCase(sortBy)) {
            wrapper.orderByDesc(BizAnime::getRating).orderByDesc(BizAnime::getCreateTime);
        } else if ("year".equalsIgnoreCase(sortBy)) {
            wrapper.orderByDesc(BizAnime::getPublishYear).orderByDesc(BizAnime::getCreateTime);
        } else {
            wrapper.orderByDesc(BizAnime::getFeatured).orderByDesc(BizAnime::getCreateTime);
        }

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<BizAnime> page =
                animeService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    /**
     * 分页获取番剧列表 (供后台管理使用)
     */
    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<BizAnime>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer year) {
        LambdaQueryWrapper<BizAnime> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            wrapper.like(BizAnime::getTitle, title);
        }
        if (year != null) {
            wrapper.eq(BizAnime::getPublishYear, year);
        }
        // 推荐番剧排在最前面，其余按创建时间倒序
        wrapper.orderByDesc(BizAnime::getFeatured).orderByDesc(BizAnime::getCreateTime);
        return Result.success(animeService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize), wrapper));
    }

    /**
     * 获取番剧详情
     */
    @GetMapping("/{id}")
    public Result<BizAnime> getDetail(@PathVariable Long id) {
        return Result.success(animeService.getById(id));
    }

    /**
     * 根据 Bangumi ID 获取详情 (如果不存在则静默同步)
     */
    @GetMapping("/bgm/{bgmId}")
    public Result<BizAnime> getDetailByBgmId(@PathVariable Integer bgmId) {
        BizAnime anime = animeService.getOne(new LambdaQueryWrapper<BizAnime>().eq(BizAnime::getBgmId, bgmId));
        if (anime == null) {
            anime = animeService.syncFromBangumi(bgmId);
        }
        return Result.success(anime);
    }

    /**
     * 触发从 Bangumi 同步数据 (管理端或内部接口使用)
     */
    @PostMapping("/sync/{bgmId}")
    public Result<BizAnime> syncFromBangumi(@PathVariable Integer bgmId) {
        BizAnime anime = animeService.syncFromBangumi(bgmId);
        return Result.success(anime);
    }

    /**
     * 更新番剧信息 (管理端编辑)
     */
    @PutMapping
    public Result<Void> update(@org.springframework.validation.annotation.Validated @RequestBody BizAnime anime) {
        animeService.updateById(anime);
        return Result.success(null);
    }

    /**
     * 删除番剧
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean removed = animeService.removeById(id);
        if (!removed) {
            return Result.error("删除失败：未找到对应番剧");
        }
        return Result.success(null);
    }

    /**
     * 切换番剧的首页轮播推荐状态（0↔1），上限 5 个
     */
    @PutMapping("/featured/{id}")
    public Result<Void> toggleFeatured(@PathVariable Long id) {
        BizAnime anime = animeService.getById(id);
        if (anime == null) {
            return Result.error("番剧不存在");
        }
        boolean isCurrentlyFeatured = anime.getFeatured() != null && anime.getFeatured() == 1;

        if (!isCurrentlyFeatured) {
            // 想要设为推荐，先检查数量
            long count = animeService.count(new LambdaQueryWrapper<BizAnime>().eq(BizAnime::getFeatured, 1));
            if (count >= 5) {
                return Result.error("首页轮播推荐最多 5 个，请先取消其他番剧的推荐");
            }
        }

        anime.setFeatured(isCurrentlyFeatured ? 0 : 1);
        animeService.updateById(anime);
        return Result.success(null);
    }

    /**
     * 获取首页轮播推荐番剧列表 (featured=1)
     */
    @GetMapping("/featured")
    public Result<List<BizAnime>> featuredList() {
        List<BizAnime> list = animeService.list(new LambdaQueryWrapper<BizAnime>()
                .eq(BizAnime::getFeatured, 1)
                .orderByDesc(BizAnime::getCreateTime));
        return Result.success(list);
    }

    /**
     * 获取每日放送表 (新番时间表)
     */
    @GetMapping("/calendar")
    public Result<com.alibaba.fastjson2.JSONArray> getCalendar() {
        return Result.success(animeService.getBangumiCalendar());
    }
    /**
     * 在 Bangumi 中搜索番剧
     */
    @GetMapping("/bangumi/search")
    public Result<com.alibaba.fastjson2.JSONObject> searchBangumi(@RequestParam String keywords) {
        return Result.success(animeService.searchBangumi(keywords));
    }

    /**
     * 批量从 Bangumi 导入番剧到本地库（管理/一键收录使用）
     */
    @PostMapping("/import")
    public Result<java.util.List<BizAnime>> importFromBangumi(@RequestBody java.util.List<Integer> bgmIds) {
        java.util.List<BizAnime> list = animeService.importFromBangumi(bgmIds);
        return Result.success(list);
    }

    /**
     * 批量更新所有番剧的类型信息（从 Bangumi 重新获取）
     */
    @PostMapping("/sync/genres")
    public Result<Integer> syncAllGenres() {
        int count = animeService.batchUpdateGenresFromBangumi();
        return Result.success(count);
    }
}
