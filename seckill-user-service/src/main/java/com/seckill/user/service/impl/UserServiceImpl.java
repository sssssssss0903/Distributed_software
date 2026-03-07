package com.seckill.user.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.common.exception.BusinessException;
import com.seckill.common.result.ResultCode;
import com.seckill.common.util.JwtUtil;
import com.seckill.user.dto.UserLoginDTO;
import com.seckill.user.dto.UserRegisterDTO;
import com.seckill.user.entity.User;
import com.seckill.user.mapper.UserMapper;
import com.seckill.user.service.UserService;
import com.seckill.user.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserLoginVO register(UserRegisterDTO dto) {
        User existUser = getUserByUsername(dto.getUsername());
        if (existUser != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS.getCode(), 
                    ResultCode.USER_ALREADY_EXISTS.getMessage());
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(1);

        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException("用户注册失败");
        }

        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());

        return new UserLoginVO(user.getId(), user.getUsername(), token);
    }

    @Override
    public UserLoginVO login(UserLoginDTO dto) {
        User user = getUserByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), 
                    ResultCode.USER_NOT_FOUND.getMessage());
        }

        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR.getCode(), 
                    ResultCode.PASSWORD_ERROR.getMessage());
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("用户已被禁用");
        }

        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

        return new UserLoginVO(user.getId(), user.getUsername(), token);
    }

    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }
}
