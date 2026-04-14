package com.seckill.order.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.seckill.common.result.Result;
import com.seckill.order.dto.SeckillRequestDTO;
import com.seckill.order.entity.Order;
import com.seckill.order.service.OrderService;
import com.seckill.order.vo.SeckillSubmitVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Api(tags = "订单管理")
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @ApiOperation("秒杀下单")
    @PostMapping("/seckill")
    @SentinelResource(value = "seckillSubmit",
            blockHandler = "seckillBlockHandler",
            fallback = "seckillFallback")
    public Result<SeckillSubmitVO> seckill(@Valid @RequestBody SeckillRequestDTO dto) {
        return Result.success(orderService.seckillSubmit(dto));
    }

    @ApiOperation("按订单ID查询")
    @GetMapping("/{orderId}")
    @SentinelResource(value = "getOrder", blockHandler = "getOrderBlockHandler")
    public Result<Order> getByOrderId(@PathVariable Long orderId) {
        return Result.success(orderService.getByOrderId(orderId));
    }

    @ApiOperation("按用户ID查询订单")
    @GetMapping("/user/{userId}")
    public Result<List<Order>> getByUserId(@PathVariable Long userId) {
        return Result.success(orderService.getByUserId(userId));
    }

    @ApiOperation("订单支付")
    @PostMapping("/pay/{orderId}")
    @SentinelResource(value = "payOrder", blockHandler = "payOrderBlockHandler")
    public Result<String> pay(@PathVariable Long orderId, @RequestParam Long userId) {
        orderService.payOrder(orderId, userId);
        return Result.success("支付请求已受理，正在处理中");
    }

    // ========== Sentinel限流降级处理方法 ==========

    /**
     * 秒杀接口限流处理
     */
    public Result<SeckillSubmitVO> seckillBlockHandler(SeckillRequestDTO dto, BlockException e) {
        log.warn("秒杀接口被限流: userId={}, productId={}, rule={}",
                dto.getUserId(), dto.getProductId(), e.getClass().getSimpleName());
        return Result.error(429, "系统繁忙，秒杀请求过于频繁，请稍后重试");
    }

    /**
     * 秒杀接口降级处理
     */
    public Result<SeckillSubmitVO> seckillFallback(SeckillRequestDTO dto, Throwable e) {
        log.error("秒杀接口降级: userId={}, productId={}, error={}",
                dto.getUserId(), dto.getProductId(), e.getMessage());
        return Result.error(503, "秒杀服务暂时不可用，请稍后重试");
    }

    /**
     * 查询订单限流处理
     */
    public Result<Order> getOrderBlockHandler(Long orderId, BlockException e) {
        log.warn("查询订单被限流: orderId={}", orderId);
        return Result.error(429, "查询过于频繁，请稍后重试");
    }

    /**
     * 支付接口限流处理
     */
    public Result<String> payOrderBlockHandler(Long orderId, Long userId, BlockException e) {
        log.warn("支付接口被限流: orderId={}, userId={}", orderId, userId);
        return Result.error(429, "支付请求过于频繁，请稍后重试");
    }
}
