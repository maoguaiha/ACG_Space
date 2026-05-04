package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 番剧追番记录实体
 */
@Data
@TableName("biz_anime_follow")
public class BizAnimeFollow {

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
     * 番剧ID
     */
    private Long animeId;

    /**
     * 追番时间
     */
    private LocalDateTime createTime;
}
