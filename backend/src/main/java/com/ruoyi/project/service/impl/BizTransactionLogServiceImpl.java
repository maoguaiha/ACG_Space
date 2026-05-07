package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.domain.entity.BizTransactionLog;
import com.ruoyi.project.mapper.BizTransactionLogMapper;
import com.ruoyi.project.service.IBizTransactionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * RocketMQ事务日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizTransactionLogServiceImpl extends ServiceImpl<BizTransactionLogMapper, BizTransactionLog>
        implements IBizTransactionLogService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLog(String transactionId, String topic, String tag, String businessType, String businessData) {
        BizTransactionLog logEntry = new BizTransactionLog();
        logEntry.setTransactionId(transactionId);
        logEntry.setTopic(topic);
        logEntry.setTag(tag);
        logEntry.setStatus(BizTransactionLog.STATUS_PREPARING);
        logEntry.setBusinessType(businessType);
        logEntry.setBusinessData(businessData);
        logEntry.setCheckCount(0);
        logEntry.setDelFlag(0);
        logEntry.setCreateTime(LocalDateTime.now());
        logEntry.setUpdateTime(LocalDateTime.now());
        
        this.save(logEntry);
        log.info("保存事务日志, transactionId: {}, businessType: {}", transactionId, businessType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markCommitted(String transactionId) {
        BizTransactionLog logEntry = getByTransactionId(transactionId);
        if (logEntry != null) {
            logEntry.setStatus(BizTransactionLog.STATUS_COMMITTED);
            logEntry.setUpdateTime(LocalDateTime.now());
            this.updateById(logEntry);
            log.info("事务已提交, transactionId: {}", transactionId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRolledBack(String transactionId) {
        BizTransactionLog logEntry = getByTransactionId(transactionId);
        if (logEntry != null) {
            logEntry.setStatus(BizTransactionLog.STATUS_ROLLED_BACK);
            logEntry.setUpdateTime(LocalDateTime.now());
            this.updateById(logEntry);
            log.info("事务已回滚, transactionId: {}", transactionId);
        }
    }

    @Override
    public BizTransactionLog getByTransactionId(String transactionId) {
        return this.getOne(new LambdaQueryWrapper<BizTransactionLog>()
                .eq(BizTransactionLog::getTransactionId, transactionId)
                .eq(BizTransactionLog::getDelFlag, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementCheckCount(String transactionId) {
        BizTransactionLog logEntry = getByTransactionId(transactionId);
        if (logEntry != null) {
            logEntry.setCheckCount(logEntry.getCheckCount() + 1);
            logEntry.setLastCheckTime(LocalDateTime.now());
            this.updateById(logEntry);
            log.info("事务回查次数+1, transactionId: {}, checkCount: {}", transactionId, logEntry.getCheckCount());
        }
    }
}