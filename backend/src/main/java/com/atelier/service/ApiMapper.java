package com.atelier.service;

import com.atelier.api.model.CartDto;
import com.atelier.api.model.CartItemDto;
import com.atelier.api.model.CategoryDto;
import com.atelier.api.model.CustomerDto;
import com.atelier.api.model.OrderDto;
import com.atelier.api.model.OrderItemDto;
import com.atelier.api.model.OrderPageDto;
import com.atelier.api.model.OrderStatusDto;
import com.atelier.api.model.ProductDto;
import com.atelier.api.model.ProductPageDto;
import com.atelier.api.model.RoleDto;
import com.atelier.domain.Cart;
import com.atelier.domain.CartItem;
import com.atelier.domain.Category;
import com.atelier.domain.Customer;
import com.atelier.domain.CustomerOrder;
import com.atelier.domain.OrderItem;
import com.atelier.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;

@Component
public class ApiMapper {

    public CustomerDto toCustomer(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setFullName(customer.getFullName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setAddress(customer.getAddress());
        dto.setRole(RoleDto.valueOf(customer.getRole().name()));
        if (customer.getCreatedAt() != null) {
            dto.setCreatedAt(OffsetDateTime.ofInstant(customer.getCreatedAt(), ZoneOffset.UTC));
        }
        return dto;
    }

    public CategoryDto toCategory(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }

    public ProductDto toProduct(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        if (product.getCreatedAt() != null) {
            dto.setCreatedAt(OffsetDateTime.ofInstant(product.getCreatedAt(), ZoneOffset.UTC));
        }
        return dto;
    }

    public ProductPageDto toProductPage(Page<Product> page) {
        ProductPageDto dto = new ProductPageDto();
        dto.setContent(page.getContent().stream().map(this::toProduct).toList());
        dto.setPage(page.getNumber());
        dto.setSize(page.getSize());
        dto.setTotalElements(page.getTotalElements());
        dto.setTotalPages(page.getTotalPages());
        return dto;
    }

    public CartDto toCart(Cart cart) {
        CartDto dto = new CartDto();
        dto.setCustomerId(cart.getCustomer().getId());
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.getItems().stream()
                .sorted(Comparator.comparing(CartItem::getId, Comparator.nullsLast(Long::compareTo)))
                .toList()) {
            BigDecimal line = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(line);
            CartItemDto lineDto = new CartItemDto();
            lineDto.setId(item.getId());
            lineDto.setProductId(item.getProduct().getId());
            lineDto.setProductName(item.getProduct().getName());
            lineDto.setImageUrl(item.getProduct().getImageUrl());
            lineDto.setUnitPrice(item.getProduct().getPrice());
            lineDto.setQuantity(item.getQuantity());
            lineDto.setLineTotal(line);
            dto.addItemsItem(lineDto);
        }
        dto.setTotalAmount(total);
        return dto;
    }

    public CartDto emptyCart(Long customerId) {
        CartDto dto = new CartDto();
        dto.setCustomerId(customerId);
        dto.setItems(java.util.List.of());
        dto.setTotalAmount(BigDecimal.ZERO);
        return dto;
    }

    public OrderDto toOrder(CustomerOrder order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setCode(order.getCode());
        dto.setCustomerId(order.getCustomer().getId());
        dto.setCustomerName(order.getCustomer().getFullName());
        dto.setStatus(OrderStatusDto.valueOf(order.getStatus().name()));
        dto.setTotalAmount(order.getTotalAmount());
        dto.setNote(order.getNote());
        if (order.getCreatedAt() != null) {
            dto.setCreatedAt(OffsetDateTime.ofInstant(order.getCreatedAt(), ZoneOffset.UTC));
        }
        for (OrderItem item : order.getItems()) {
            OrderItemDto line = new OrderItemDto();
            line.setId(item.getId());
            line.setProductId(item.getProduct().getId());
            line.setProductName(item.getProductName());
            line.setUnitPrice(item.getUnitPrice());
            line.setQuantity(item.getQuantity());
            line.setLineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            dto.addItemsItem(line);
        }
        return dto;
    }

    public OrderPageDto toOrderPage(Page<CustomerOrder> page) {
        OrderPageDto dto = new OrderPageDto();
        dto.setContent(page.getContent().stream().map(this::toOrder).toList());
        dto.setPage(page.getNumber());
        dto.setSize(page.getSize());
        dto.setTotalElements(page.getTotalElements());
        dto.setTotalPages(page.getTotalPages());
        return dto;
    }
}
