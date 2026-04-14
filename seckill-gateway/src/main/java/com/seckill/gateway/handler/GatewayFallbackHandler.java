package com.seckill.gateway.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 网关降级处理器
 * 当下游服务不可用时返回友好的降级响应
 */
@Slf4j
@Component
public class GatewayFallbackHandler implements HandlerFunction<ServerResponse> {

    @Override
    public Mono<ServerResponse> handle(ServerRequest request) {
        Route route = (Route) request.exchange()
                .getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String serviceId = route != null ? route.getId() : "unknown";

        log.warn("服务降级触发: serviceId={}, path={}", serviceId, request.path());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 503);
        result.put("message", "服务暂时不可用，已启用降级保护，请稍后重试");
        result.put("data", null);

        return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(result));
    }
}
