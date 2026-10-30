package com.atelier.web;

import com.atelier.api.ProductsApi;
import com.atelier.api.model.ProductDto;
import com.atelier.api.model.ProductPageDto;
import com.atelier.api.model.ProductWriteDto;
import com.atelier.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class ProductController implements ProductsApi {

    private final ProductService products;

    public ProductController(ProductService products) {
        this.products = products;
    }

    @Override
    public ResponseEntity<ProductPageDto> listProducts(String q, Long categoryId, BigDecimal minPrice,
                                                       BigDecimal maxPrice, Integer page, Integer size) {
        return ResponseEntity.ok(products.list(q, categoryId, minPrice, maxPrice, page, size));
    }

    @Override
    public ResponseEntity<ProductDto> getProduct(Long productId) {
        return ResponseEntity.ok(products.get(productId));
    }

    @Override
    public ResponseEntity<ProductDto> createProduct(ProductWriteDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(products.create(request));
    }

    @Override
    public ResponseEntity<ProductDto> updateProduct(Long productId, ProductWriteDto request) {
        return ResponseEntity.ok(products.update(productId, request));
    }

    @Override
    public ResponseEntity<Void> deleteProduct(Long productId) {
        products.delete(productId);
        return ResponseEntity.noContent().build();
    }
}
