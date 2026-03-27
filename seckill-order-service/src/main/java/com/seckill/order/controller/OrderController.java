package com.seckill.order.controller;

import com.seckill.common.result.Result;
import com.seckill.order.dto.SeckillRequestDTO;
import com.seckill.order.entity.Order;
import com.seckill.order.service.OrderService;
import com.seckill.order.vo.SeckillSubmitVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "订单管理")
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @ApiOperation("秒杀下单")
    @PostMapping("/seckill")
    public Result<SeckillSubmitVO> seckill(@Valid @RequestBody SeckillRequestDTO dto) {
        return Result.success(orderService.seckillSubmit(dto));
    }

    @ApiOperation("按订单ID查询")
    @GetMapping("/{orderId}")
    public Result<Order> getByOrderId(@PathVariable Long orderId) {
        return Result.success(orderService.getByOrderId(orderId));
    }

    @ApiOperation("按用户ID查询订单")
    @GetMapping("/user/{userId}")
    public Result<List<Order>> getByUserId(@PathVariable Long userId) {
        return Result.success(orderService.getByUserId(userId));
    }
}
