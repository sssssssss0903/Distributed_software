package com.seckill.product.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.seckill.product.entity.Product;
import com.seckill.product.mapper.ProductMapper;
import com.seckill.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 商品服务 - Redis缓存实现
 * 防护: 缓存穿透(空值缓存)、缓存击穿(互斥锁)、缓存雪崩(随机TTL)
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private static final String CACHE_KEY_PREFIX = "product:detail:";
    private static final String NULL_CACHE_PREFIX = "product:null:";
    private static final String LOCK_PREFIX = "product:lock:";
    private static final long CACHE_TTL_SECONDS = 300;      // 基础5分钟
    private static final long NULL_CACHE_TTL = 60;          // 空值缓存1分钟(防穿透)
    private static final long LOCK_EXPIRE_SECONDS = 10;     // 锁10秒超时

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    @DS("slave")
    public Product getProductDetail(Long productId) {
        if (productId == null || productId <= 0) {
            return null;
        }
        String cacheKey = CACHE_KEY_PREFIX + productId;
        String nullKey = NULL_CACHE_PREFIX + productId;

        // 1. 查Redis缓存
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("缓存命中: productId={}", productId);
            return JSON.parseObject(cached, Product.class);
        }

        // 2. 防穿透: 检查空值缓存(不存在的数据)
        if (Boolean.TRUE.equals(redisTemplate.hasKey(nullKey))) {
            log.debug("空值缓存命中(防穿透): productId={}", productId);
            return null;
        }

        // 3. 防击穿: 互斥锁
        String lockKey = LOCK_PREFIX + productId;
        try {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(locked)) {
                try {
                    // 双重检查
                    cached = redisTemplate.opsForValue().get(cacheKey);
                    if (cached != null) {
                        return JSON.parseObject(cached, Product.class);
                    }
                    // 查DB（走从库）
                    Product product = productMapper.selectById(productId);
                    if (product != null) {
                        // 防雪崩: 随机TTL (240-360秒)
                        long ttl = CACHE_TTL_SECONDS + (long) (Math.random() * 120);
                        redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(product), ttl, TimeUnit.SECONDS);
                        return product;
                    } else {
                        // 防穿透: 缓存空值
                        redisTemplate.opsForValue().set(nullKey, "1", NULL_CACHE_TTL, TimeUnit.SECONDS);
                        return null;
                    }
                } finally {
                    redisTemplate.delete(lockKey);
                }
            } else {
                // 未获取锁，等待后重试
                Thread.sleep(50);
                return getProductDetail(productId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return productMapper.selectById(productId);
        }
    }

    @Override
    @DS("slave")
    public List<Product> listProducts() {
        return productMapper.selectList(null);
    }

    @Override
    @DS("master")
    public Product createProduct(Product product) {
        productMapper.insert(product);
        log.info("商品写入主库: productId={}", product.getId());
        return product;
    }
}
