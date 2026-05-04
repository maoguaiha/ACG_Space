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

    /** 是否本人 */
    private Boolean isSelf;
    /** 当前登录用户是否已关注（仅非本人时有效） */
    private Boolean isFollowed;
}
