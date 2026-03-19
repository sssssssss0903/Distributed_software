package com.seckill.user.controller;

import com.seckill.common.result.Result;
import com.seckill.common.util.JwtUtil;
import com.seckill.user.dto.UserLoginDTO;
import com.seckill.user.dto.UserRegisterDTO;
import com.seckill.user.entity.User;
import com.seckill.user.service.UserService;
import com.seckill.user.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@Slf4j
@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @org.springframework.beans.factory.annotation.Value("${server.port:8081}")
    private String serverPort;

    @ApiOperation("健康检查/实例信息(用于负载均衡验证)")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> info = new HashMap<>();
        info.put("port", serverPort);
        info.put("status", "UP");
        info.put("service", "seckill-user-service");
        log.info("健康检查请求 - 实例端口: {}", serverPort);
        return Result.success(info);
    }

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<UserLoginVO> register(@Valid @RequestBody UserRegisterDTO dto) {
        log.info("用户注册请求: username={}", dto.getUsername());
        UserLoginVO vo = userService.register(dto);
        return Result.success(vo);
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO dto) {
        log.info("用户登录请求: username={}", dto.getUsername());
        UserLoginVO vo = userService.login(dto);
        return Result.success(vo);
    }

    @ApiOperation("获取用户信息")
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = JwtUtil.getUserIdFromToken(token);
        User user = userService.getUserById(userId);
        user.setPassword(null);
        return Result.success(user);
    }

    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        String username = JwtUtil.getUsernameFromToken(token);
        log.info("用户登出: username={}", username);
        return Result.success("登出成功", null);
    }
}
