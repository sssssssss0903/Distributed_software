package com.seckill.product.service.impl;

import com.seckill.product.document.ProductDocument;
import com.seckill.product.entity.Product;
import com.seckill.product.mapper.ProductMapper;
import com.seckill.product.repository.ProductSearchRepository;
import com.seckill.product.service.ProductSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductSearchServiceImpl implements ProductSearchService {

    @Autowired
    private ProductSearchRepository searchRepository;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<ProductDocument> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return searchRepository.findAll();
        }
        return searchRepository.findByNameContainingOrDescriptionContaining(keyword, keyword);
    }

    @Override
    public void syncProduct(Product product) {
        if (product == null) return;
        ProductDocument doc = toDocument(product);
        searchRepository.save(doc);
        log.info("同步商品到ES: id={}, name={}", product.getId(), product.getName());
    }

    @Override
    @com.baomidou.dynamic.datasource.annotation.DS("slave")
    public void syncAll() {
        List<Product> products = productMapper.selectList(null);
        List<ProductDocument> docs = products.stream().map(this::toDocument).collect(Collectors.toList());
        searchRepository.saveAll(docs);
        log.info("全量同步商品到ES: {} 条", docs.size());
    }

    private ProductDocument toDocument(Product p) {
        ProductDocument doc = new ProductDocument();
        doc.setId(p.getId());
        doc.setName(p.getName());
        doc.setDescription(p.getDescription());
        doc.setPrice(p.getPrice());
        doc.setCategoryId(p.getCategoryId());
        doc.setStatus(p.getStatus());
        doc.setCreateTime(p.getCreateTime());
        return doc;
    }
}
