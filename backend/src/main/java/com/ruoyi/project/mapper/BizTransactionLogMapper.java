package com.ruoyi.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.project.domain.entity.BizTransactionLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * RocketMQ事务日志Mapper
 */
@Mapper
public interface BizTransactionLogMapper extends BaseMapper<BizTransactionLog> {
}