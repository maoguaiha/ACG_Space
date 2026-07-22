package com.ruoyi.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.project.common.annotation.Idempotent;
import com.ruoyi.project.common.annotation.RateLimiterAndCircuitBreaker;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.vo.MarketItemVO;
import com.ruoyi.project.service.IBizMarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 市场控制器
 */
@Tag(name = "市场交易", description = "资产上架、购买(含1%手续费)、下架、市场浏览")
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
    @Operation(summary = "购买商品", description = "购买市场中的商品，自动扣1%手续费转给卖家，转移资产所有权。含幂等性保护和限流熔断。")
    @PostMapping("/buy")
    @Idempotent(prefix = "market_buy", expireTime = 10, message = "购买操作过于频繁，请稍后再试")
    @RateLimiterAndCircuitBreaker(rateLimiterName = "marketBuy", circuitBreakerName = "marketService")
    public Result<OrderResult> buy(@RequestBody @Validated BuyRequest request) {
        Long buyerId = getCurrentUserId();
        String orderId = marketService.buyItem(buyerId, request.getItemId());
        OrderResult result = new OrderResult();
        result.setOrderId(orderId);
        return Result.success(result);
    }

    /**
     * 上架资产到市场
     */
    @PostMapping("/list")
    @Idempotent(prefix = "market_list", expireTime = 10, message = "上架操作过于频繁，请稍后再试")
    public Result<ListResult> listAsset(@RequestBody @Validated ListRequest request) {
        Long userId = getCurrentUserId();
        Long itemId = marketService.listAsset(userId, request.getAssetId(), request.getPrice());
        ListResult result = new ListResult();
        result.setItemId(itemId);
        return Result.success(result);
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
        Long userId = getCurrentUserId();
        boolean success = marketService.delistAsset(id, userId);
        return Result.success(success);
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