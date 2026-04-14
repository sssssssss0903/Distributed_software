package com.seckill.order.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单服务Sentinel流量治理规则配置
 * 服务端限流 + 熔断降级
 */
@Slf4j
@Configuration
public class SentinelConfig {

    @PostConstruct
    public void initRules() {
        initFlowRules();
        initDegradeRules();
        log.info("订单服务Sentinel流量治理规则初始化完成");
    }

    /**
     * 限流规则
     */
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 秒杀接口 - QPS限流=50
        FlowRule seckillRule = new FlowRule();
        seckillRule.setResource("seckillSubmit");
        seckillRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        seckillRule.setCount(50);
        seckillRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(seckillRule);

        // 查询订单 - QPS限流=200
        FlowRule getOrderRule = new FlowRule();
        getOrderRule.setResource("getOrder");
        getOrderRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        getOrderRule.setCount(200);
        rules.add(getOrderRule);

        // 支付接口 - QPS限流=100
        FlowRule payRule = new FlowRule();
        payRule.setResource("payOrder");
        payRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        payRule.setCount(100);
        rules.add(payRule);

        FlowRuleManager.loadRules(rules);
        log.info("订单服务限流规则加载: {} 条", rules.size());
    }

    /**
     * 熔断降级规则
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 秒杀接口 - 慢调用比例熔断 (RT > 200ms, 比例 > 50%, 熔断10秒)
        DegradeRule seckillSlowRule = new DegradeRule("seckillSubmit")
                .setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType())
                .setCount(0.5)
                .setSlowRatioThreshold(0.5)
                .setTimeWindow(10)
                .setMinRequestAmount(5)
                .setStatIntervalMs(10000);
        rules.add(seckillSlowRule);

        // 秒杀接口 - 异常比例熔断 (异常比例 > 50%, 熔断15秒)
        DegradeRule seckillErrorRule = new DegradeRule("seckillSubmit")
                .setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType())
                .setCount(0.5)
                .setTimeWindow(15)
                .setMinRequestAmount(5)
                .setStatIntervalMs(10000);
        rules.add(seckillErrorRule);

        DegradeRuleManager.loadRules(rules);
        log.info("订单服务熔断规则加载: {} 条", rules.size());
    }
}
