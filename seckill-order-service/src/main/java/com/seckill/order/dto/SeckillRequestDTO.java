package com.seckill.order.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class SeckillRequestDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Min(value = 1, message = "购买数量至少为1")
    private Integer quantity = 1;
}
