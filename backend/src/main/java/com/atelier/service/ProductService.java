package com.atelier.service;

import com.atelier.api.model.ProductDto;
import com.atelier.api.model.ProductPageDto;
import com.atelier.api.model.ProductWriteDto;
import com.atelier.domain.Category;
import com.atelier.domain.Product;
import com.atelier.error.ApiException;
import com.atelier.repo.CartItemRepository;
import com.atelier.repo.CategoryRepository;
import com.atelier.repo.OrderItemRepository;
import com.atelier.repo.ProductRepository;
import com.atelier.security.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository products;
    private final CategoryRepository categories;
    private final OrderItemRepository orderItems;
    private final CartItemRepository cartItems;
    private final ApiMapper mapper;

    public ProductService(ProductRepository products, CategoryRepository categories,
                          OrderItemRepository orderItems, CartItemRepository cartItems, ApiMapper mapper) {
        this.products = products;
        this.categories = categories;
        this.orderItems = orderItems;
        this.cartItems = cartItems;
        this.mapper = mapper;
    }

    public ProductPageDto list(String q, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
                               Integer page, Integer size) {
        String keyword = q == null ? null : q.trim();
        return mapper.toProductPage(products.search(keyword, categoryId, minPrice, maxPrice, Pages.of(page, size)));
    }

    public ProductDto get(Long productId) {
        return mapper.toProduct(require(productId));
    }

    @Transactional
    public ProductDto create(ProductWriteDto request) {
        AuthContext.requireAdmin();
        Product product = new Product();
        apply(product, request);
        product.setCreatedAt(Instant.now());
        return mapper.toProduct(products.save(product));
    }

    @Transactional
    public ProductDto update(Long productId, ProductWriteDto request) {
        AuthContext.requireAdmin();
        Product product = require(productId);
        apply(product, request);
        return mapper.toProduct(product);
    }

    @Transactional
    public void delete(Long productId) {
        AuthContext.requireAdmin();
        Product product = require(productId);
        if (orderItems.existsByProductId(productId) || cartItems.existsByProductId(productId)) {
            throw ApiException.badRequest("ERR_PRODUCT_IN_USE", "Sản phẩm đang nằm trong giỏ hoặc đơn, không thể xóa");
        }
        products.delete(product);
    }

    private void apply(Product product, ProductWriteDto request) {
        Category category = categories.findById(request.getCategoryId())
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy danh mục"));
        product.setName(request.getName().trim());
        product.setDescription(Texts.blankToNull(request.getDescription()));
        product.setPrice(request.getPrice().setScale(0, RoundingMode.HALF_UP));
        product.setImageUrl(Texts.blankToNull(request.getImageUrl()));
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);
    }

    private Product require(Long productId) {
        return products.findById(productId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy sản phẩm"));
    }
}
