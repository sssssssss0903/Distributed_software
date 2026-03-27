package com.seckill.order.mq;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.common.dto.SeckillOrderMessage;
import com.seckill.order.entity.Order;
import com.seckill.order.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
public class OrderCreateConsumer {

    private static final String TOPIC_CREATED = "seckill.order.created";
    private static final String TOPIC_ROLLBACK = "seckill.order.rollback";

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "seckill.order.create", groupId = "seckill-order-create")
    public void consumeCreate(String payload) {
        SeckillOrderMessage message = JSON.parseObject(payload, SeckillOrderMessage.class);
        if (message == null || message.getOrderId() == null) {
            return;
        }

        if (orderMapper.selectById(message.getOrderId()) != null) {
            log.info("重复消费创建消息, orderId={}", message.getOrderId());
            return;
        }

        Order order = new Order();
        order.setId(message.getOrderId());
        order.setOrderNo("SK" + message.getOrderId());
        order.setUserId(message.getUserId());
        order.setProductId(message.getProductId());
        order.setProductName("SECKILL_PRODUCT_" + message.getProductId());
        order.setPrice(BigDecimal.ZERO);
        order.setQuantity(message.getQuantity());
        order.setTotalAmount(BigDecimal.ZERO);
        order.setStatus(0);
        order.setIsSeckill(1);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        try {
            orderMapper.insert(order);
            kafkaTemplate.send(TOPIC_CREATED, payload);
            log.info("订单创建成功, orderId={}", message.getOrderId());
        } catch (DuplicateKeyException duplicateKeyException) {
            log.warn("订单幂等命中, userId={}, productId={}", message.getUserId(), message.getProductId());
        } catch (Exception e) {
            log.error("订单创建异常, orderId={}", message.getOrderId(), e);
            message.setReason("ORDER_CREATE_FAILED");
            kafkaTemplate.send(TOPIC_ROLLBACK, JSON.toJSONString(message));
        }
    }

    @KafkaListener(topics = "seckill.order.rollback", groupId = "seckill-order-rollback")
    public void consumeRollback(String payload) {
        SeckillOrderMessage message = JSON.parseObject(payload, SeckillOrderMessage.class);
        if (message == null || message.getOrderId() == null) {
            return;
        }

        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getId, message.getOrderId()).set(Order::getStatus, 2).set(Order::getUpdateTime, LocalDateTime.now());
        orderMapper.update(null, wrapper);
        log.warn("订单回滚完成, orderId={}, reason={}", message.getOrderId(), message.getReason());
    }
}
