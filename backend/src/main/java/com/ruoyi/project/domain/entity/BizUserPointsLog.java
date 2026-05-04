package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户积分流水实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_user_points_log")
public class BizUserPointsLog extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 动作类型 (如 COMMENT, LOGIN, SHARE)
     */
    private String actionType;

    /**
     * 积分变动 (正负值)
     */
    private Integer pointsChange;

    /**
     * 业务关联ID (防止重复发放的幂等键)
     */
    private String bizReferenceId;
}
