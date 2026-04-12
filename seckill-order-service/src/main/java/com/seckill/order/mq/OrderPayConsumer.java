package com.seckill.order.mq;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.common.dto.SeckillOrderMessage;
import com.seckill.order.entity.Order;
import com.seckill.order.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class OrderPayConsumer {

    private static final String TOPIC_PAID = "seckill.order.paid";
    private static final String TOPIC_ROLLBACK = "seckill.order.rollback";

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "seckill.order.pay", groupId = "seckill-order-pay")
    public void consumePay(String payload) {
        SeckillOrderMessage message = JSON.parseObject(payload, SeckillOrderMessage.class);
        if (message == null || message.getOrderId() == null) {
            return;
        }

        Order order = orderMapper.selectById(message.getOrderId());
        if (order == null) {
            log.warn("支付消费：订单不存在, orderId={}", message.getOrderId());
            return;
        }

        if (order.getStatus() == 1) {
            log.info("订单已支付，幂等跳过, orderId={}", message.getOrderId());
            return;
        }

        if (order.getStatus() != 0) {
            log.warn("订单状态异常，无法支付, orderId={}, status={}", message.getOrderId(), order.getStatus());
            return;
        }

        try {
            // 模拟支付处理（实际对接支付网关）
            boolean paySuccess = processPayment(order);

            if (paySuccess) {
                LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(Order::getId, message.getOrderId())
                        .eq(Order::getStatus, 0)
                        .set(Order::getStatus, 1)
                        .set(Order::getPayTime, LocalDateTime.now())
                        .set(Order::getUpdateTime, LocalDateTime.now());
                int affected = orderMapper.update(null, wrapper);

                if (affected > 0) {
                    message.setAction("PAID");
                    kafkaTemplate.send(TOPIC_PAID, JSON.toJSONString(message));
                    log.info("订单支付成功, orderId={}", message.getOrderId());
                } else {
                    log.warn("订单支付状态更新失败（并发冲突）, orderId={}", message.getOrderId());
                }
            } else {
                // 支付失败，取消订单并回滚库存
                LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(Order::getId, message.getOrderId())
                        .set(Order::getStatus, 3)
                        .set(Order::getUpdateTime, LocalDateTime.now());
                orderMapper.update(null, wrapper);

                message.setReason("PAY_FAILED");
                kafkaTemplate.send(TOPIC_ROLLBACK, JSON.toJSONString(message));
                log.warn("订单支付失败，触发回滚, orderId={}", message.getOrderId());
            }
        } catch (Exception e) {
            log.error("支付处理异常, orderId={}", message.getOrderId(), e);
            message.setReason("PAY_EXCEPTION");
            kafkaTemplate.send(TOPIC_ROLLBACK, JSON.toJSONString(message));
        }
    }

    private boolean processPayment(Order order) {
        // 模拟支付网关调用，实际场景对接第三方支付
        log.info("调用支付网关, orderId={}, amount={}", order.getId(), order.getTotalAmount());
        return true;
    }
}
