package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户碎片实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_user_fragment")
public class BizUserFragment extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String fragmentType;

    private Integer quantity;
}
