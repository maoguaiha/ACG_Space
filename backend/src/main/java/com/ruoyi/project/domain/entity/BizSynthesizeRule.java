package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 合成规则实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_synthesize_rule")
public class BizSynthesizeRule extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String sourceRarity;

    private Integer sourceCount;

    private String targetRarity;

    private Integer targetCount;

    private Integer isPhysical;

    private Integer status;
}
