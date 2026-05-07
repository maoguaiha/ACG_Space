package com.ruoyi.project.domain.vo;

import lombok.Data;

/**
 * 用户公开资料 VO（不包含密码等敏感信息）
 */
@Data
public class UserProfileVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String bio;
    private Integer points;
    private Integer followerCount;
    private Integer followingCount;

    /** VIP状态 (0=无VIP,1=VIP,2=SVIP) */
    private Integer vipStatus;
    /** VIP到期时间 */
    private java.time.LocalDateTime vipExpireTime;
    /** 用户等级 (1-100) */
    private Integer userLevel;
    /** 当前经验值 */
    private Integer levelExperience;

    /** 是否本人 */
    private Boolean isSelf;
    /** 当前登录用户是否已关注（仅非本人时有效） */
    private Boolean isFollowed;
}
