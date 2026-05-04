package com.ruoyi.project.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVO {

    private Long id;
    private Long fromUserId;
    private String fromUsername;
    private String fromNickname;
    private String fromAvatar;
    private Long toUserId;
    private String toUsername;
    private String toNickname;
    private String toAvatar;
    private String content;
    private Boolean isRead;
    private LocalDateTime createTime;
}
