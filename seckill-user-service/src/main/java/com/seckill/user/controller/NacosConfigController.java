package com.seckill.user.controller;

import com.seckill.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Nacos配置中心动态属性演示
 * 通过@RefreshScope实现配置动态刷新，无需重启服务
 */
@Slf4j
@Api(tags = "Nacos配置管理")
@RestController
@RequestMapping("/api/config")
@RefreshScope
public class NacosConfigController {

    @Value("${seckill.max-purchase-limit:1}")
    private Integer maxPurchaseLimit;

    @Value("${seckill.activity-name:默认秒杀活动}")
    private String activityName;

    @Value("${seckill.enable:true}")
    private Boolean seckillEnable;

    @Value("${seckill.rate-limit:100}")
    private Integer rateLimit;

    @ApiOperation("获取当前Nacos动态配置")
    @GetMapping("/info")
    public Result<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("maxPurchaseLimit", maxPurchaseLimit);
        config.put("activityName", activityName);
        config.put("seckillEnable", seckillEnable);
        config.put("rateLimit", rateLimit);
        log.info("读取Nacos动态配置: maxPurchaseLimit={}, activityName={}, seckillEnable={}, rateLimit={}",
                maxPurchaseLimit, activityName, seckillEnable, rateLimit);
        return Result.success(config);
    }

    @ApiOperation("测试配置动态刷新")
    @GetMapping("/refresh-test")
    public Result<String> refreshTest() {
        String msg = String.format("当前活动: %s, 限购: %d件, 秒杀开关: %s, 限流QPS: %d",
                activityName, maxPurchaseLimit, seckillEnable ? "开启" : "关闭", rateLimit);
        log.info("配置刷新测试: {}", msg);
        return Result.success(msg);
    }
}
