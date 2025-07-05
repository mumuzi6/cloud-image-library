package com.kryos.cloudImagebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新用户个人资料请求
 */
@Data
public class UserUpdateProfileRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    private static final long serialVersionUID = 1L;
}