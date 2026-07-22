package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizItem;

import java.util.List;

public interface IBizItemService extends IService<BizItem> {

    Page<BizItem> pageItems(long pageNum, long pageSize, String name, String rarity, String type, String itemKey);

    BizItem getByItemKey(String itemKey);

    boolean createItem(BizItem item);

    boolean updateItem(BizItem item);

    boolean deleteItem(Long id);

    boolean decrementStock(Long itemId, int quantity);

    boolean incrementStock(Long itemId, int quantity);

    List<BizItem> listByRarity(String rarity);
}
