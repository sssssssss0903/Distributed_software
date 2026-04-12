package com.seckill.order.scheduler;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.common.dto.SeckillOrderMessage;
import com.seckill.order.entity.Order;
import com.seckill.order.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderTimeoutScheduler {

    private static final int PAY_TIMEOUT_MINUTES = 30;
    private static final String TOPIC_ROLLBACK = "seckill.order.rollback";

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedRate = 60000)
    public void cancelTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(PAY_TIMEOUT_MINUTES);

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getStatus, 0)
                .lt(Order::getCreateTime, deadline);

        List<Order> timeoutOrders = orderMapper.selectList(queryWrapper);
        for (Order order : timeoutOrders) {
            LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Order::getId, order.getId())
                    .eq(Order::getStatus, 0)
                    .set(Order::getStatus, 4)
                    .set(Order::getUpdateTime, LocalDateTime.now());
            int affected = orderMapper.update(null, updateWrapper);

            if (affected > 0) {
                SeckillOrderMessage message = new SeckillOrderMessage();
                message.setOrderId(order.getId());
                message.setUserId(order.getUserId());
                message.setProductId(order.getProductId());
                message.setQuantity(order.getQuantity());
                message.setReason("PAY_TIMEOUT");
                kafkaTemplate.send(TOPIC_ROLLBACK, JSON.toJSONString(message));
                log.info("订单支付超时自动取消, orderId={}", order.getId());
            }
        }
    }
}
