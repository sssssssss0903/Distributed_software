package com.seckill.inventory.mq;

import com.alibaba.fastjson2.JSON;
import com.seckill.common.dto.SeckillOrderMessage;
import com.seckill.inventory.mapper.InventoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryDeductConsumer {

    private static final String TOPIC_ROLLBACK = "seckill.order.rollback";

    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "seckill.order.created", groupId = "seckill-inventory-deduct")
    public void consumeOrderCreated(String payload) {
        SeckillOrderMessage message = JSON.parseObject(payload, SeckillOrderMessage.class);
        if (message == null || message.getProductId() == null || message.getQuantity() == null) {
            return;
        }

        int affected = inventoryMapper.deduct(message.getProductId(), message.getQuantity());
        if (affected <= 0) {
            message.setReason("INVENTORY_DEDUCT_FAILED");
            kafkaTemplate.send(TOPIC_ROLLBACK, JSON.toJSONString(message));
            log.warn("数据库库存扣减失败, productId={}, orderId={}", message.getProductId(), message.getOrderId());
            return;
        }

        log.info("数据库库存扣减成功, productId={}, orderId={}", message.getProductId(), message.getOrderId());
    }
}
