package com.seckill.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 用户登录请求DTO
 */
@Data
public class UserLoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
