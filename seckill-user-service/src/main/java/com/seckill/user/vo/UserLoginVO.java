package com.seckill.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户登录响应VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * JWT Token
     */
    private String token;
}
