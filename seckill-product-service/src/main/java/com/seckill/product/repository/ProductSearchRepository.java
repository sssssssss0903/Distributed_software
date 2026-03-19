package com.seckill.product.repository;

import com.seckill.product.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {

    List<ProductDocument> findByNameContaining(String name);

    List<ProductDocument> findByNameContainingOrDescriptionContaining(String name, String description);
}
