package com.seckill.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Sentinel网关流量治理配置
 * 包含：限流规则、熔断降级规则、自定义API分组、自定义限流响应
 */
@Slf4j
@Configuration
public class SentinelGatewayConfig {

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    public SentinelGatewayConfig(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                  ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    /**
     * Sentinel异常处理器
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    /**
     * Sentinel全局过滤器
     */
    @Bean
    @Order(-1)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    /**
     * 初始化Sentinel规则
     */
    @PostConstruct
    public void initRules() {
        initCustomApis();
        initFlowRules();
        initDegradeRules();
        initBlockHandler();
        log.info("Sentinel网关流量治理规则初始化完成");
    }

    /**
     * 自定义API分组 - 按业务维度分组
     */
    private void initCustomApis() {
        Set<ApiDefinition> definitions = new HashSet<>();

        // 秒杀API分组
        ApiDefinition seckillApi = new ApiDefinition("seckill-api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem()
                            .setPattern("/api/order/seckill")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_EXACT));
                }});

        // 用户API分组
        ApiDefinition userApi = new ApiDefinition("user-api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem()
                            .setPattern("/api/user/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});

        // 商品API分组
        ApiDefinition productApi = new ApiDefinition("product-api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem()
                            .setPattern("/api/product/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});

        definitions.add(seckillApi);
        definitions.add(userApi);
        definitions.add(productApi);
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
        log.info("自定义API分组加载完成: {}", definitions.size());
    }

    /**
     * 限流规则
     * - 秒杀接口：QPS=50，快速失败
     * - 用户接口：QPS=200
     * - 商品接口：QPS=300
     * - 按路由ID全局限流
     */
    private void initFlowRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();

        // 秒杀接口限流 - QPS=50（核心保护）
        rules.add(new GatewayFlowRule("seckill-api")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(50)
                .setIntervalSec(1));

        // 用户服务路由限流 - QPS=200
        rules.add(new GatewayFlowRule("seckill-user-service")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_ROUTE_ID)
                .setCount(200)
                .setIntervalSec(1));

        // 商品服务路由限流 - QPS=300
        rules.add(new GatewayFlowRule("seckill-product-service")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_ROUTE_ID)
                .setCount(300)
                .setIntervalSec(1));

        // 订单服务路由限流 - QPS=100
        rules.add(new GatewayFlowRule("seckill-order-service")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_ROUTE_ID)
                .setCount(100)
                .setIntervalSec(1));

        GatewayRuleManager.loadRules(rules);
        log.info("限流规则加载完成: {}", rules.size());
    }

    /**
     * 熔断降级规则
     * - 慢调用比例熔断：RT>500ms且比例>50%，熔断10秒
     * - 异常比例熔断：异常比例>50%，熔断10秒
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 订单服务 - 慢调用比例熔断
        DegradeRule orderSlowRule = new DegradeRule("seckill-order-service")
                .setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType())
                .setCount(0.5)                 // 慢调用比例阈值50%
                .setSlowRatioThreshold(0.5)    // 触发比例
                .setTimeWindow(10)             // 熔断时长10秒
                .setMinRequestAmount(5)        // 最小请求数
                .setStatIntervalMs(10000);     // 统计时长10秒

        // 订单服务 - 异常比例熔断
        DegradeRule orderErrorRule = new DegradeRule("seckill-order-service")
                .setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType())
                .setCount(0.5)                 // 异常比例阈值50%
                .setTimeWindow(10)             // 熔断时长10秒
                .setMinRequestAmount(5)
                .setStatIntervalMs(10000);

        // 商品服务 - 慢调用比例熔断
        DegradeRule productSlowRule = new DegradeRule("seckill-product-service")
                .setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType())
                .setCount(0.5)
                .setSlowRatioThreshold(0.5)
                .setTimeWindow(10)
                .setMinRequestAmount(5)
                .setStatIntervalMs(10000);

        rules.add(orderSlowRule);
        rules.add(orderErrorRule);
        rules.add(productSlowRule);
        DegradeRuleManager.loadRules(rules);
        log.info("熔断降级规则加载完成: {}", rules.size());
    }

    /**
     * 自定义限流/熔断后的响应
     */
    private void initBlockHandler() {
        BlockRequestHandler blockRequestHandler = (serverWebExchange, throwable) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 429);
            result.put("message", "系统繁忙，请稍后重试（已触发流量治理）");
            result.put("data", null);

            log.warn("请求被Sentinel拦截: path={}, rule={}",
                    serverWebExchange.getRequest().getURI().getPath(),
                    throwable.getClass().getSimpleName());

            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(result));
        };
        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
    }
}
