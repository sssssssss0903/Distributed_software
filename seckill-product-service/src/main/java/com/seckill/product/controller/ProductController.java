package com.seckill.product.controller;

import com.seckill.common.result.Result;
import com.seckill.product.document.ProductDocument;
import com.seckill.product.entity.Product;
import com.seckill.product.service.ProductSearchService;
import com.seckill.product.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(tags = "商品管理")
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired(required = false)
    private ProductSearchService productSearchService;

    @Value("${server.port:8083}")
    private String serverPort;

    @ApiOperation("商品详情(Redis缓存)")
    @GetMapping("/{id}")
    public Result<Product> getDetail(@PathVariable Long id) {
        Product product = productService.getProductDetail(id);
        return Result.success(product);
    }

    @ApiOperation("商品列表(读从库)")
    @GetMapping("/list")
    public Result<List<Product>> list() {
        return Result.success(productService.listProducts());
    }

    @ApiOperation("新增商品(写主库)")
    @PostMapping
    public Result<Product> create(@RequestBody Product product) {
        Product created = productService.createProduct(product);
        if (productSearchService != null) {
            productSearchService.syncProduct(created);
        }
        return Result.success(created);
    }

    @ApiOperation("商品搜索(ES)")
    @GetMapping("/search")
    public Result<java.util.List<ProductDocument>> search(@RequestParam(required = false) String keyword) {
        if (productSearchService == null) {
            return Result.error("Elasticsearch 未配置");
        }
        return Result.success(productSearchService.search(keyword));
    }

    @ApiOperation("同步商品到ES")
    @PostMapping("/sync-es")
    public Result<String> syncToEs() {
        if (productSearchService == null) {
            return Result.error("Elasticsearch 未配置");
        }
        productSearchService.syncAll();
        return Result.success("同步完成");
    }

    @ApiOperation("健康检查")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> info = new HashMap<>();
        info.put("port", serverPort);
        info.put("status", "UP");
        info.put("service", "seckill-product-service");
        log.info("健康检查 - 端口: {}", serverPort);
        return Result.success(info);
    }
}
