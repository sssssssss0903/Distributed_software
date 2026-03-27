package com.seckill.order.mq;

import com.alibaba.fastjson2.JSON;
import com.seckill.common.dto.SeckillOrderMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderRollbackRedisConsumer {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @KafkaListener(topics = "seckill.order.rollback", groupId = "seckill-order-rollback-redis")
    public void consumeRollback(String payload) {
        SeckillOrderMessage message = JSON.parseObject(payload, SeckillOrderMessage.class);
        if (message == null || message.getUserId() == null || message.getProductId() == null || message.getQuantity() == null) {
            return;
        }

        stringRedisTemplate.opsForValue().increment(stockKey(message.getProductId()), message.getQuantity());
        stringRedisTemplate.delete(userProductKey(message.getUserId(), message.getProductId()));
        log.warn("Redis库存补偿完成, orderId={}", message.getOrderId());
    }

    private String stockKey(Long productId) {
        return "seckill:stock:" + productId;
    }

    private String userProductKey(Long userId, Long productId) {
        return "seckill:order:user:" + userId + ":" + productId;
    }
}
