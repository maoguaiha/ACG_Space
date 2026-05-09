package com.ruoyi.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.entity.BizRechargeOrder;
import com.ruoyi.project.service.IBizRechargeOrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/recharge")
@RequiredArgsConstructor
@Validated
public class BizRechargeController {

    private final IBizRechargeOrderService rechargeOrderService;

    @GetMapping("/packages")
    public Result<List<RechargePackage>> getPackages() {
        List<RechargePackage> packages = new ArrayList<>();
        packages.add(new RechargePackage(new BigDecimal("1"), 10, "新手体验"));
        packages.add(new RechargePackage(new BigDecimal("10"), 100, "基础套餐"));
        packages.add(new RechargePackage(new BigDecimal("50"), 550, "超值套餐"));
        packages.add(new RechargePackage(new BigDecimal("100"), 1200, "豪华套餐"));
        return Result.success(packages);
    }

    @PostMapping("/create")
    public Result<CreateOrderResult> createOrder(@RequestBody @Validated CreateOrderRequest request) {
        Long userId = getCurrentUserId();
        BizRechargeOrder order = rechargeOrderService.createOrder(userId, request.getAmount(), request.getPoints());
        CreateOrderResult result = new CreateOrderResult();
        result.setOrderNo(order.getOrderNo());
        return Result.success(result);
    }

    @PostMapping("/mock-pay")
    public Result<Boolean> mockPay(@RequestBody @Validated MockPayRequest request) {
        try {
            boolean success = rechargeOrderService.mockPaySuccess(request.getOrderNo());
            return Result.success(success);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/orders")
    public Result<Page<BizRechargeOrder>> getOrders(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        Long userId = getCurrentUserId();
        Page<BizRechargeOrder> page = rechargeOrderService.pageOrders(userId, pageNum, pageSize);
        return Result.success(page);
    }

    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            return 1L;
        }
    }

    @Data
    public static class RechargePackage {
        private BigDecimal amount;
        private Integer points;
        private String name;
        public RechargePackage(BigDecimal amount, Integer points, String name) {
            this.amount = amount;
            this.points = points;
            this.name = name;
        }
    }

    @Data
    public static class CreateOrderRequest {
        private BigDecimal amount;
        private Integer points;
    }

    @Data
    public static class CreateOrderResult {
        private String orderNo;
    }

    @Data
    public static class MockPayRequest {
        private String orderNo;
    }
}
