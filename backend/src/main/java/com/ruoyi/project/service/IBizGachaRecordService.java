package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizGachaRecord;

public interface IBizGachaRecordService extends IService<BizGachaRecord> {

    Page<BizGachaRecord> pageRecords(long pageNum, long pageSize, Long userId);

    boolean saveRecord(BizGachaRecord record);
}
