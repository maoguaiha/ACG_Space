package com.ruoyi.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /**
     * 用户ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 密码 (序列化时忽略)
     */
    @JSONField(serialize = false)
    private String password;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /** 总积分 */
    private Integer points;

    /** 个人简介 */
    private String bio;

    /** 粉丝数 */
    private Integer followerCount;

    /** 关注数 */
    private Integer followingCount;

    /** VIP状态 (0=无VIP,1=VIP,2=SVIP) */
    private Integer vipStatus;

    /** VIP到期时间 */
    private java.time.LocalDateTime vipExpireTime;

    /** 用户等级 (1-100) */
    private Integer userLevel;

    /** 当前经验值 */
    private Integer levelExperience;
}
