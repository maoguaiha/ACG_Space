package com.ruoyi.project.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserLikeHistoryVO {
    private Long id;
    private Integer type;
    private Long targetId;
    private String targetTitle;
    private String targetCover;
    private LocalDateTime createTime;
}