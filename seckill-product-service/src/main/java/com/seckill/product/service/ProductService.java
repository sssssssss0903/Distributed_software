package com.seckill.product.service;

import com.seckill.product.entity.Product;

import java.util.List;

public interface ProductService {

    /**
     * 获取商品详情（含缓存穿透/击穿/雪崩防护，读走从库）
     */
    Product getProductDetail(Long productId);

    /**
     * 商品列表（读走从库）
     */
    List<Product> listProducts();

    /**
     * 新增商品（写走主库）
     */
    Product createProduct(Product product);
}
