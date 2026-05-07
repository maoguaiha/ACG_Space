package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * RocketMQ事务日志回查表
 */
@Data
@TableName("biz_transaction_log")
public class BizTransactionLog {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * RocketMQ事务消息ID
     */
    private String transactionId;

    /**
     * 消息主题
     */
    private String topic;

    /**
     * 消息标签
     */
    private String tag;

    /**
     * 事务状态 (0准备中 1提交 2回滚)
     */
    private Integer status;

    /**
     * 业务类型 (TRADE_BUY, TRADE_SELL等)
     */
    private String businessType;

    /**
     * 业务数据JSON
     */
    private String businessData;

    /**
     * 回查次数
     */
    private Integer checkCount;

    /**
     * 最后回查时间
     */
    private LocalDateTime lastCheckTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建者
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private Integer delFlag;

    /**
     * 事务状态常量
     */
    public static final Integer STATUS_PREPARING = 0;
    public static final Integer STATUS_COMMITTED = 1;
    public static final Integer STATUS_ROLLED_BACK = 2;
}