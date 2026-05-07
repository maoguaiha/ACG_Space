package com.ruoyi.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.domain.entity.BizDeliveryOrder;
import com.ruoyi.project.service.IBizDeliveryOrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
@Validated
public class BizDeliveryController {

    private final IBizDeliveryOrderService deliveryOrderService;

    /**
     * 分页获取核销订单列表 (管理端)
     */
    @GetMapping("/page")
    public Result<Page<BizDeliveryOrder>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) Integer status) {
        Page<BizDeliveryOrder> page = deliveryOrderService.pageOrders(pageNum, pageSize, orderId, status);
        return Result.success(page);
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = deliveryOrderService.getStats();
        return Result.success(stats);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public Result<BizDeliveryOrder> getByOrderId(@PathVariable String orderId) {
        BizDeliveryOrder order = deliveryOrderService.getByOrderId(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    /**
     * 发货
     */
    @PostMapping("/ship")
    public Result<Boolean> ship(@RequestBody @Validated ShipRequest request) {
        boolean success = deliveryOrderService.ship(
                request.getOrderId(),
                request.getExpressCompany(),
                request.getExpressNo(),
                request.getRemark()
        );
        if (success) {
            return Result.success(true);
        }
        return Result.error("发货失败");
    }

    /**
     * 确认收货
     */
    @PostMapping("/{orderId}/complete")
    public Result<Boolean> complete(@PathVariable String orderId) {
        boolean success = deliveryOrderService.complete(orderId);
        if (success) {
            return Result.success(true);
        }
        return Result.error("确认收货失败");
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    public Result<Boolean> cancel(@PathVariable String orderId) {
        boolean success = deliveryOrderService.cancel(orderId);
        if (success) {
            return Result.success(true);
        }
        return Result.error("取消订单失败");
    }

    @Data
    public static class ShipRequest {
        private String orderId;
        private String expressCompany;
        private String expressNo;
        private String remark;
    }
}