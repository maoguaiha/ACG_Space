package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.domain.entity.BizUserPointsLog;
import com.ruoyi.project.mapper.BizUserPointsLogMapper;
import com.ruoyi.project.service.IBizUserPointsLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class BizUserPointsLogServiceImpl extends ServiceImpl<BizUserPointsLogMapper, BizUserPointsLog> implements IBizUserPointsLogService {

    private static final String ACTION_COMMENT = "COMMENT";
    private static final int POINTS_PER_COMMENT = 5;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPointsForComment(Long userId, Long commentId) {
        String referenceId = String.valueOf(commentId);
        
        BizUserPointsLog pointsLog = new BizUserPointsLog();
        pointsLog.setUserId(userId);
        pointsLog.setActionType(ACTION_COMMENT);
        pointsLog.setPointsChange(POINTS_PER_COMMENT);
        pointsLog.setBizReferenceId(referenceId);
        pointsLog.setRemark("发表评论奖励积分");

        try {
            // 利用数据库唯一索引 (action_type, biz_reference_id) 实现硬核幂等性校验
            this.save(pointsLog);
            
            // 当前积分汇总策略：按需聚合（通过 SUM(points_change) 查流水表）。
            // 若后续建立 biz_user 总积分字段，可在此处改为 UPDATE biz_user SET total_points = total_points + POINTS_PER_COMMENT WHERE id = userId
            log.info("用户 [{}] 成功增加 {} 积分, 来源评论 ID: {}", userId, POINTS_PER_COMMENT, commentId);
            
        } catch (DuplicateKeyException e) {
            // 捕获唯一索引冲突异常，说明该评论之前已经计算过积分，直接丢弃（不抛出异常，让 MQ 认为消费成功）
            log.warn("触发防重校验：用户 [{}] 的评论 [{}] 积分已发放，忽略此次 MQ 消息", userId, commentId);
        }
    }
}
