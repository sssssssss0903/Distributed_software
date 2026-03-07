package com.seckill.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    TOKEN_INVALID(1004, "Token无效"),
    TOKEN_EXPIRED(1005, "Token已过期"),
    
    PRODUCT_NOT_FOUND(2001, "商品不存在"),
    PRODUCT_OFF_SHELF(2002, "商品已下架"),
    
    STOCK_NOT_ENOUGH(3001, "库存不足"),
    STOCK_DEDUCT_FAILED(3002, "库存扣减失败"),
    
    ORDER_NOT_FOUND(4001, "订单不存在"),
    ORDER_CREATE_FAILED(4002, "订单创建失败"),
    ORDER_STATUS_ERROR(4003, "订单状态错误"),
    
    SECKILL_NOT_START(5001, "秒杀未开始"),
    SECKILL_ENDED(5002, "秒杀已结束"),
    SECKILL_SOLD_OUT(5003, "秒杀商品已售罄"),
    SECKILL_REPEAT(5004, "请勿重复秒杀"),
    SECKILL_LIMIT(5005, "秒杀请求过于频繁");

    private final Integer code;
    private final String message;
}
