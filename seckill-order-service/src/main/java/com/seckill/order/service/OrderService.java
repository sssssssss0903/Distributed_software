package com.seckill.order.service;

import com.seckill.order.dto.SeckillRequestDTO;
import com.seckill.order.entity.Order;
import com.seckill.order.vo.SeckillSubmitVO;

import java.util.List;

public interface OrderService {

    SeckillSubmitVO seckillSubmit(SeckillRequestDTO dto);

    Order getByOrderId(Long orderId);

    List<Order> getByUserId(Long userId);
}
