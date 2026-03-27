package com.seckill.order.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.common.dto.SeckillOrderMessage;
import com.seckill.common.exception.BusinessException;
import com.seckill.common.result.ResultCode;
import com.seckill.order.dto.SeckillRequestDTO;
import com.seckill.order.entity.Order;
import com.seckill.order.mapper.OrderMapper;
import com.seckill.order.service.OrderService;
import com.seckill.order.util.SnowflakeIdGenerator;
import com.seckill.order.vo.SeckillSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private static final String TOPIC_CREATE = "seckill.order.create";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;
    @Autowired
    private OrderMapper orderMapper;

    @Override
    public SeckillSubmitVO seckillSubmit(SeckillRequestDTO dto) {
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "购买数量不合法");
        }

        String userProductKey = userProductKey(dto.getUserId(), dto.getProductId());
        Boolean first = stringRedisTemplate.opsForValue()
                .setIfAbsent(userProductKey, "1", 1, TimeUnit.DAYS);
        if (Boolean.FALSE.equals(first)) {
            throw new BusinessException(ResultCode.SECKILL_REPEAT.getCode(), ResultCode.SECKILL_REPEAT.getMessage());
        }

        Long remain = deductStockFromRedis(dto.getProductId(), dto.getQuantity());
        if (remain == null || remain < 0) {
            stringRedisTemplate.delete(userProductKey);
            throw new BusinessException(ResultCode.SECKILL_SOLD_OUT.getCode(), ResultCode.SECKILL_SOLD_OUT.getMessage());
        }

        long orderId = snowflakeIdGenerator.nextId();
        SeckillOrderMessage message = new SeckillOrderMessage();
        message.setOrderId(orderId);
        message.setUserId(dto.getUserId());
        message.setProductId(dto.getProductId());
        message.setQuantity(dto.getQuantity());

        try {
            kafkaTemplate.send(TOPIC_CREATE, JSON.toJSONString(message));
        } catch (Exception e) {
            log.error("发送下单消息失败, orderId={}", orderId, e);
            rollbackRedis(dto.getUserId(), dto.getProductId(), dto.getQuantity());
            throw new BusinessException(ResultCode.ORDER_CREATE_FAILED.getCode(), "秒杀排队失败，请重试");
        }

        return new SeckillSubmitVO(orderId, "秒杀请求已受理，订单异步创建中");
    }

    @Override
    public Order getByOrderId(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND.getCode(), ResultCode.ORDER_NOT_FOUND.getMessage());
        }
        return order;
    }

    @Override
    public List<Order> getByUserId(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId).orderByDesc(Order::getCreateTime);
        return orderMapper.selectList(wrapper);
    }

    private Long deductStockFromRedis(Long productId, Integer quantity) {
        String script =
                "local stock = redis.call('GET', KEYS[1]);" +
                "if (not stock) then return -1 end;" +
                "stock = tonumber(stock);" +
                "local q = tonumber(ARGV[1]);" +
                "if (stock < q) then return -1 end;" +
                "return redis.call('DECRBY', KEYS[1], q);";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);
        return stringRedisTemplate.execute(redisScript,
                Collections.singletonList(stockKey(productId)), String.valueOf(quantity));
    }

    private void rollbackRedis(Long userId, Long productId, Integer quantity) {
        stringRedisTemplate.opsForValue().increment(stockKey(productId), quantity);
        stringRedisTemplate.delete(userProductKey(userId, productId));
    }

    private String stockKey(Long productId) {
        return "seckill:stock:" + productId;
    }

    private String userProductKey(Long userId, Long productId) {
        return "seckill:order:user:" + userId + ":" + productId;
    }
}
