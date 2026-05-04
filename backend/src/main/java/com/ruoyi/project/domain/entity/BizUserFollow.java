package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户关注关系实体
 */
@Data
@TableName("biz_user_follow")
public class BizUserFollow {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关注者ID */
    private Long userId;

    /** 被关注者ID */
    private Long followUserId;

    /** 关注时间 */
    private LocalDateTime createTime;
}
