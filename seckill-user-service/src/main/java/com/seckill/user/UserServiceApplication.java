package com.seckill.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.seckill.user", "com.seckill.common"})
@EnableDiscoveryClient
@MapperScan("com.seckill.user.mapper")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
