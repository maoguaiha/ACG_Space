package com.ruoyi.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.project.common.annotation.Idempotent;
import com.ruoyi.project.common.annotation.RateLimiterAndCircuitBreaker;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.vo.MarketItemVO;
import com.ruoyi.project.service.IBizMarketService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 市场控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
@Validated
public class BizMarketController {

    private final IBizMarketService marketService;

    /**
     * 分页获取市场商品列表
     */
    @GetMapping("/page")
    public Result<Page<MarketItemVO>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String itemType,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String sortBy) {

        Page<MarketItemVO> page = marketService.pageItems(
                pageNum, pageSize, itemName, itemType, rarity, minPrice, maxPrice, sortBy);
        return Result.success(page);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public Result<MarketItemVO> getById(@PathVariable Long id) {
        MarketItemVO item = marketService.getItemById(id);
        if (item == null) {
            return Result.error("商品不存在");
        }
        return Result.success(item);
    }

    /**
     * 购买商品
     */
    @PostMapping("/buy")
    @Idempotent(prefix = "market_buy", expireTime = 10, message = "购买操作过于频繁，请稍后再试")
    @RateLimiterAndCircuitBreaker(rateLimiterName = "marketBuy", circuitBreakerName = "marketService")
    public Result<OrderResult> buy(@RequestBody @Validated BuyRequest request) {
        try {
            Long buyerId = getCurrentUserId();
            String orderId = marketService.buyItem(buyerId, request.getItemId());
            OrderResult result = new OrderResult();
            result.setOrderId(orderId);
            return Result.success(result);
        } catch (RuntimeException e) {
            log.error("购买失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上架资产到市场
     */
    @PostMapping("/list")
    @Idempotent(prefix = "market_list", expireTime = 10, message = "上架操作过于频繁，请稍后再试")
    public Result<ListResult> listAsset(@RequestBody @Validated ListRequest request) {
        try {
            Long userId = getCurrentUserId();
            Long itemId = marketService.listAsset(userId, request.getAssetId(), request.getPrice());
            ListResult result = new ListResult();
            result.setItemId(itemId);
            return Result.success(result);
        } catch (RuntimeException e) {
            log.error("上架失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户的市场挂单列表
     */
    @GetMapping("/my-listings")
    public Result<List<MarketItemVO>> getMyListings() {
        Long userId = getCurrentUserId();
        List<MarketItemVO> listings = marketService.getUserListings(userId);
        return Result.success(listings);
    }

    /**
     * 下架商品
     */
    @PostMapping("/delist/{id}")
    public Result<Boolean> delist(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            boolean success = marketService.delistAsset(id, userId);
            return Result.success(success);
        } catch (RuntimeException e) {
            log.error("下架失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    private Long getCurrentUserId() {
        try {
            Long userId = SecurityUtils.getUserId();
            return userId != null ? userId : 1L;
        } catch (Exception e) {
            return 1L;
        }
    }

    @Data
    public static class OrderResult {
        private String orderId;
    }

    @Data
    public static class ListResult {
        private Long itemId;
    }

    @Data
    public static class BuyRequest {
        private Long itemId;
    }

    @Data
    public static class ListRequest {
        private Long assetId;
        private Integer price;
    }
}