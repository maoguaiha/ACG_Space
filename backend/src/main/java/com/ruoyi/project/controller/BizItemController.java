package com.ruoyi.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.domain.entity.BizItem;
import com.ruoyi.project.service.IBizItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/item")
@RequiredArgsConstructor
@Validated
public class BizItemController {

    private final IBizItemService itemService;

    /**
     * 分页获取物品列表 (管理端)
     */
    @GetMapping("/page")
    public Result<Page<BizItem>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String type) {
        Page<BizItem> page = itemService.pageItems(pageNum, pageSize, name, rarity, type);
        return Result.success(page);
    }

    /**
     * 获取物品详情
     */
    @GetMapping("/{id}")
    public Result<BizItem> getById(@PathVariable Long id) {
        BizItem item = itemService.getById(id);
        if (item == null) {
            return Result.error("物品不存在");
        }
        return Result.success(item);
    }

    /**
     * 根据itemKey获取物品
     */
    @GetMapping("/key/{itemKey}")
    public Result<BizItem> getByItemKey(@PathVariable String itemKey) {
        BizItem item = itemService.getByItemKey(itemKey);
        if (item == null) {
            return Result.error("物品不存在");
        }
        return Result.success(item);
    }

    /**
     * 创建物品
     */
    @PostMapping
    public Result<Boolean> create(@RequestBody BizItem item) {
        boolean success = itemService.createItem(item);
        if (success) {
            return Result.success(true);
        }
        return Result.error("创建失败");
    }

    /**
     * 更新物品
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody BizItem item) {
        if (item.getId() == null) {
            return Result.error("物品ID不能为空");
        }
        boolean success = itemService.updateItem(item);
        if (success) {
            return Result.success(true);
        }
        return Result.error("更新失败");
    }

    /**
     * 删除物品
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = itemService.deleteItem(id);
        if (success) {
            return Result.success(true);
        }
        return Result.error("删除失败");
    }
}