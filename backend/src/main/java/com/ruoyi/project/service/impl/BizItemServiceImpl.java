package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.domain.entity.BizItem;
import com.ruoyi.project.mapper.BizItemMapper;
import com.ruoyi.project.service.IBizItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizItemServiceImpl extends ServiceImpl<BizItemMapper, BizItem> implements IBizItemService {

    private final RedissonClient redissonClient;

    private static final String STOCK_LOCK_PREFIX = "item:stock:lock:";

    @Override
    public Page<BizItem> pageItems(long pageNum, long pageSize, String name, String rarity, String type) {
        Page<BizItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizItem::getDelFlag, 0);

        if (name != null && !name.isBlank()) {
            wrapper.like(BizItem::getName, name);
        }
        if (rarity != null && !rarity.isBlank()) {
            wrapper.eq(BizItem::getRarity, rarity);
        }
        if (type != null && !type.isBlank()) {
            wrapper.eq(BizItem::getType, type);
        }

        wrapper.orderByDesc(BizItem::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public BizItem getByItemKey(String itemKey) {
        return this.getOne(new LambdaQueryWrapper<BizItem>()
                .eq(BizItem::getItemKey, itemKey)
                .eq(BizItem::getDelFlag, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createItem(BizItem item) {
        item.setDelFlag(0);
        item.setCreateTime(java.time.LocalDateTime.now());
        item.setUpdateTime(java.time.LocalDateTime.now());
        if (item.getTotalStock() == null) {
            item.setTotalStock(0);
        }
        if (item.getRemainingStock() == null) {
            item.setRemainingStock(item.getTotalStock());
        }
        return this.save(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateItem(BizItem item) {
        item.setUpdateTime(java.time.LocalDateTime.now());
        return this.updateById(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteItem(Long id) {
        BizItem item = this.getById(id);
        if (item != null) {
            item.setDelFlag(2);
            item.setUpdateTime(java.time.LocalDateTime.now());
            return this.updateById(item);
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decrementStock(Long itemId, int quantity) {
        String lockKey = STOCK_LOCK_PREFIX + itemId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                log.warn("获取库存锁失败, itemId: {}", itemId);
                return false;
            }

            try {
                BizItem item = this.getById(itemId);
                if (item == null || item.getRemainingStock() < quantity) {
                    log.warn("库存不足或物品不存在, itemId: {}, currentStock: {}, need: {}",
                            itemId, item != null ? item.getRemainingStock() : 0, quantity);
                    return false;
                }

                item.setRemainingStock(item.getRemainingStock() - quantity);
                item.setUpdateTime(java.time.LocalDateTime.now());
                return this.updateById(item);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("获取库存锁被中断, itemId: {}", itemId, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incrementStock(Long itemId, int quantity) {
        String lockKey = STOCK_LOCK_PREFIX + itemId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                log.warn("获取库存锁失败, itemId: {}", itemId);
                return false;
            }

            try {
                BizItem item = this.getById(itemId);
                if (item == null) {
                    return false;
                }

                item.setRemainingStock(item.getRemainingStock() + quantity);
                if (item.getRemainingStock() > item.getTotalStock()) {
                    item.setRemainingStock(item.getTotalStock());
                }
                item.setUpdateTime(java.time.LocalDateTime.now());
                return this.updateById(item);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("获取库存锁被中断, itemId: {}", itemId, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public List<BizItem> listByRarity(String rarity) {
        LambdaQueryWrapper<BizItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizItem::getDelFlag, 0);
        if (rarity != null && !rarity.isBlank()) {
            wrapper.eq(BizItem::getRarity, rarity);
        }
        wrapper.orderByDesc(BizItem::getCreateTime);
        return this.list(wrapper);
    }
}
