package com.seckill.product.service;

import com.seckill.product.document.ProductDocument;
import com.seckill.product.entity.Product;

import java.util.List;

public interface ProductSearchService {

    /**
     * 搜索商品
     */
    List<ProductDocument> search(String keyword);

    /**
     * 同步商品到 ES
     */
    void syncProduct(Product product);

    /**
     * 全量同步
     */
    void syncAll();
}
