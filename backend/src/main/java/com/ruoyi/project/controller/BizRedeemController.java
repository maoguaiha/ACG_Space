package com.ruoyi.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.entity.BizRedeemOrder;
import com.ruoyi.project.service.IBizRedeemOrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/redeem")
@RequiredArgsConstructor
@Validated
public class BizRedeemController {

    private final IBizRedeemOrderService redeemOrderService;

    @PostMapping("/create")
    public Result<CreateOrderResult> createOrder(@RequestBody @Validated CreateOrderRequest request) {
        Long userId = getCurrentUserId();
        try {
            BizRedeemOrder order = redeemOrderService.createOrder(
                    userId,
                    request.getAssetId(),
                    request.getReceiver(),
                    request.getPhone(),
                    request.getProvince(),
                    request.getCity(),
                    request.getDistrict(),
                    request.getAddress()
            );
            CreateOrderResult result = new CreateOrderResult();
            result.setOrderNo(order.getOrderNo());
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/my-orders")
    public Result<Page<BizRedeemOrder>> getMyOrders(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status) {
        Long userId = getCurrentUserId();
        Page<BizRedeemOrder> page = redeemOrderService.pageUserOrders(userId, pageNum, pageSize, status);
        return Result.success(page);
    }

    @GetMapping("/order/{orderNo}")
    public Result<BizRedeemOrder> getOrderByNo(@PathVariable String orderNo) {
        Long userId = getCurrentUserId();
        Page<BizRedeemOrder> page = redeemOrderService.pageUserOrders(userId, 1, 100, null);
        BizRedeemOrder order = page.getRecords().stream()
                .filter(o -> orderNo.equals(o.getOrderNo()))
                .findFirst()
                .orElse(null);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @GetMapping("/order/id/{id}")
    public Result<BizRedeemOrder> getOrderById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        BizRedeemOrder order = redeemOrderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            return 1L;
        }
    }

    @Data
    public static class CreateOrderRequest {
        private Long assetId;
        private String receiver;
        private String phone;
        private String province;
        private String city;
        private String district;
        private String address;
    }

    @Data
    public static class CreateOrderResult {
        private String orderNo;
    }
}
