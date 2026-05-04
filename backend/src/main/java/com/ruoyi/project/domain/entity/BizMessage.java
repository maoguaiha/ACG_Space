package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_message")
public class BizMessage {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    private Boolean isRead;

    private LocalDateTime createTime;
}
