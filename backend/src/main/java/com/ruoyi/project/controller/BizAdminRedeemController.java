package com.ruoyi.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.domain.entity.BizRedeemOrder;
import com.ruoyi.project.service.IBizRedeemOrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/redeem")
@RequiredArgsConstructor
@Validated
public class BizAdminRedeemController {

    private final IBizRedeemOrderService redeemOrderService;

    @GetMapping("/orders")
    public Result<Page<BizRedeemOrder>> getOrders(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status) {
        Page<BizRedeemOrder> page = redeemOrderService.pageAdminOrders(pageNum, pageSize, status);
        return Result.success(page);
    }

    @GetMapping("/order/{id}")
    public Result<BizRedeemOrder> getOrderById(@PathVariable Long id) {
        BizRedeemOrder order = redeemOrderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 待发货数量 (status=0)
        long pending = redeemOrderService.count(new LambdaQueryWrapper<BizRedeemOrder>()
                .eq(BizRedeemOrder::getStatus, 0)
                .eq(BizRedeemOrder::getDelFlag, 0));
        stats.put("pending", pending);
        
        // 已发货数量 (status=1)
        long shipped = redeemOrderService.count(new LambdaQueryWrapper<BizRedeemOrder>()
                .eq(BizRedeemOrder::getStatus, 1)
                .eq(BizRedeemOrder::getDelFlag, 0));
        stats.put("shipped", shipped);
        
        // 已完成数量 (status=2)
        long completed = redeemOrderService.count(new LambdaQueryWrapper<BizRedeemOrder>()
                .eq(BizRedeemOrder::getStatus, 2)
                .eq(BizRedeemOrder::getDelFlag, 0));
        stats.put("completed", completed);
        
        // 本月兑换数量
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long monthlyRedeem = redeemOrderService.count(new LambdaQueryWrapper<BizRedeemOrder>()
                .ge(BizRedeemOrder::getCreateTime, startOfMonth)
                .eq(BizRedeemOrder::getDelFlag, 0));
        stats.put("monthlyRedeem", monthlyRedeem);
        
        return Result.success(stats);
    }

    @PostMapping("/ship")
    public Result<Boolean> ship(@RequestBody @Validated ShipRequest request) {
        try {
            boolean success = redeemOrderService.updateLogistics(
                    request.getOrderId(),
                    request.getLogisticsCompany(),
                    request.getLogisticsNo()
            );
            return Result.success(success);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/complete/{id}")
    public Result<Boolean> complete(@PathVariable Long id) {
        BizRedeemOrder order = redeemOrderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        order.setStatus(2);
        order.setCompleteTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        boolean success = redeemOrderService.updateById(order);
        return Result.success(success);
    }

    @Data
    public static class ShipRequest {
        private Long orderId;
        private String logisticsCompany;
        private String logisticsNo;
    }
}
