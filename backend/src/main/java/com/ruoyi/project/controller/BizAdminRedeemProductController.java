package com.ruoyi.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.domain.entity.BizRedeemProduct;
import com.ruoyi.project.service.IBizRedeemProductService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/redeem-product")
@RequiredArgsConstructor
@Validated
public class BizAdminRedeemProductController {

    private final IBizRedeemProductService redeemProductService;

    @GetMapping("/list")
    public Result<Page<BizRedeemProduct>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status) {
        log.info("=== 兑换商品列表查询开始 ===");
        log.info("查询参数: pageNum={}, pageSize={}, status={}", pageNum, pageSize, status);
        Page<BizRedeemProduct> page = redeemProductService.pageProducts(pageNum, pageSize, status);
        log.info("查询结果: 记录数={}, 总数={}", page.getRecords().size(), page.getTotal());
        log.info("=== 兑换商品列表查询结束 ===");
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<BizRedeemProduct> getById(@PathVariable Long id) {
        BizRedeemProduct product = redeemProductService.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }

    @PostMapping("/create")
    public Result<BizRedeemProduct> create(@RequestBody @Validated ProductRequest request) {
        BizRedeemProduct product = new BizRedeemProduct();
        product.setName(request.getName());
        product.setImage(request.getImage());
        product.setDescription(request.getDescription());
        product.setUrFragmentCost(request.getUrFragmentCost());
        product.setPointsCost(request.getPointsCost());
        product.setStock(request.getStock());
        product.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        product.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        product.setExchangedCount(0);
        product.setDelFlag(0);
        product.setCreateTime(java.time.LocalDateTime.now());
        product.setUpdateTime(java.time.LocalDateTime.now());
        redeemProductService.save(product);
        return Result.success(product);
    }

    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated ProductRequest request) {
        if (request.getId() == null) {
            return Result.error("商品ID不能为空");
        }
        BizRedeemProduct product = redeemProductService.getById(request.getId());
        if (product == null) {
            return Result.error("商品不存在");
        }
        product.setName(request.getName());
        product.setImage(request.getImage());
        product.setDescription(request.getDescription());
        product.setUrFragmentCost(request.getUrFragmentCost());
        product.setPointsCost(request.getPointsCost());
        product.setStock(request.getStock());
        product.setStatus(request.getStatus());
        product.setSortOrder(request.getSortOrder());
        product.setUpdateTime(java.time.LocalDateTime.now());
        boolean success = redeemProductService.updateById(product);
        return Result.success(success);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        log.info("=== 删除商品开始 ===");
        log.info("删除ID: {}", id);
        BizRedeemProduct product = redeemProductService.getById(id);
        if (product == null) {
            log.warn("商品不存在: {}", id);
            return Result.error("商品不存在");
        }
        log.info("找到商品: id={}, name={}, delFlag={}", product.getId(), product.getName(), product.getDelFlag());
        
        // 使用 LambdaUpdateWrapper 显式更新 del_flag，因为 updateById 会忽略 @TableLogic 字段
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<BizRedeemProduct> updateWrapper = 
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        updateWrapper.eq(BizRedeemProduct::getId, id)
                     .set(BizRedeemProduct::getDelFlag, 2)
                     .set(BizRedeemProduct::getUpdateTime, java.time.LocalDateTime.now());
        boolean success = redeemProductService.update(updateWrapper);
        
        log.info("删除结果: success={}", success);
        log.info("=== 删除商品结束 ===");
        return Result.success(success);
    }

    @Data
    public static class ProductRequest {
        private Long id;
        private String name;
        private String image;
        private String description;
        private Integer urFragmentCost;
        private Integer pointsCost;
        private Integer stock;
        private Integer status;
        private Integer sortOrder;
    }
}
