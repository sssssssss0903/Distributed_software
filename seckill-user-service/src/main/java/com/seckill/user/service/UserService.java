package com.seckill.user.service;

import com.seckill.user.dto.UserLoginDTO;
import com.seckill.user.dto.UserRegisterDTO;
import com.seckill.user.entity.User;
import com.seckill.user.vo.UserLoginVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    UserLoginVO register(UserRegisterDTO dto);

    /**
     * 用户登录
     */
    UserLoginVO login(UserLoginDTO dto);

    /**
     * 根据ID获取用户信息
     */
    User getUserById(Long userId);

    /**
     * 根据用户名获取用户
     */
    User getUserByUsername(String username);
}
