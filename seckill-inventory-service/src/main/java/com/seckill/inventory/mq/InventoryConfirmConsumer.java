package com.seckill.inventory.mq;

import com.alibaba.fastjson2.JSON;
import com.seckill.common.dto.SeckillOrderMessage;
import com.seckill.inventory.mapper.InventoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryConfirmConsumer {

    @Autowired
    private InventoryMapper inventoryMapper;

    @KafkaListener(topics = "seckill.order.paid", groupId = "seckill-inventory-confirm")
    public void consumeOrderPaid(String payload) {
        SeckillOrderMessage message = JSON.parseObject(payload, SeckillOrderMessage.class);
        if (message == null || message.getProductId() == null) {
            return;
        }

        // 支付成功后，将锁定库存转为已售出（确认扣减）
        int affected = inventoryMapper.confirmDeduct(message.getProductId(), message.getQuantity());
        if (affected > 0) {
            log.info("库存确认扣减成功, productId={}, orderId={}", message.getProductId(), message.getOrderId());
        } else {
            log.warn("库存确认扣减失败, productId={}, orderId={}", message.getProductId(), message.getOrderId());
        }
    }
}
