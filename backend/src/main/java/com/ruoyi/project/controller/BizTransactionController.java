package com.ruoyi.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.domain.entity.BizTransaction;
import com.ruoyi.project.service.IBizTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
@Validated
public class BizTransactionController {

    private final IBizTransactionService transactionService;

    /**
     * 分页获取交易订单列表 (管理端)
     */
    @GetMapping("/page")
    public Result<Page<BizTransaction>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) Integer status) {
        Page<BizTransaction> page = transactionService.pageTransactions(pageNum, pageSize, orderId, status);
        return Result.success(page);
    }

    /**
     * 获取今日统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getTodayStats() {
        Map<String, Object> stats = transactionService.getTodayStats();
        return Result.success(stats);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public Result<BizTransaction> getByOrderId(@PathVariable String orderId) {
        BizTransaction transaction = transactionService.getByOrderId(orderId);
        if (transaction == null) {
            return Result.error("订单不存在");
        }
        return Result.success(transaction);
    }

    /**
     * 人工补偿处理
     */
    @PostMapping("/{orderId}/compensate")
    public Result<Boolean> compensate(@PathVariable String orderId) {
        boolean success = transactionService.compensate(orderId);
        if (success) {
            return Result.success(true);
        }
        return Result.error("补偿处理失败");
    }

    /**
     * 重试处理
     */
    @PostMapping("/{orderId}/retry")
    public Result<Boolean> retry(@PathVariable String orderId) {
        boolean success = transactionService.retry(orderId);
        if (success) {
            return Result.success(true);
        }
        return Result.error("重试处理失败");
    }
}