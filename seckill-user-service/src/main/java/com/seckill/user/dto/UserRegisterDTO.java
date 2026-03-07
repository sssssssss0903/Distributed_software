package com.seckill.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 用户注册请求DTO
 */
@Data
public class UserRegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String email;

    private String phone;
}
