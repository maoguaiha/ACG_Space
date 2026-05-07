package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.domain.entity.BizGachaRecord;
import com.ruoyi.project.mapper.BizGachaRecordMapper;
import com.ruoyi.project.service.IBizGachaRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizGachaRecordServiceImpl extends ServiceImpl<BizGachaRecordMapper, BizGachaRecord> implements IBizGachaRecordService {

    @Override
    public Page<BizGachaRecord> pageRecords(long pageNum, long pageSize, Long userId) {
        Page<BizGachaRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizGachaRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizGachaRecord::getDelFlag, 0);

        if (userId != null) {
            wrapper.eq(BizGachaRecord::getUserId, userId);
        }

        wrapper.orderByDesc(BizGachaRecord::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public boolean saveRecord(BizGachaRecord record) {
        record.setDelFlag(0);
        record.setCreateTime(java.time.LocalDateTime.now());
        record.setUpdateTime(java.time.LocalDateTime.now());
        return this.save(record);
    }
}
