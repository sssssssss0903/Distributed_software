package com.seckill.order.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeckillSubmitVO {
    private Long orderId;
    private String message;
}
