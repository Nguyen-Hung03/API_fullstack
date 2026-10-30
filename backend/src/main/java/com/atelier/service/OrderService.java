package com.atelier.service;

import com.atelier.api.model.CheckoutRequestDto;
import com.atelier.api.model.OrderDto;
import com.atelier.api.model.OrderPageDto;
import com.atelier.api.model.OrderStatusDto;
import com.atelier.api.model.OrderStatusUpdateDto;
import com.atelier.domain.Cart;
import com.atelier.domain.CartItem;
import com.atelier.domain.Customer;
import com.atelier.domain.CustomerOrder;
import com.atelier.domain.OrderItem;
import com.atelier.domain.OrderStatus;
import com.atelier.domain.Product;
import com.atelier.error.ApiException;
import com.atelier.repo.CartRepository;
import com.atelier.repo.CustomerOrderRepository;
import com.atelier.repo.CustomerRepository;
import com.atelier.repo.ProductRepository;
import com.atelier.security.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final CustomerRepository customers;
    private final CartRepository carts;
    private final ProductRepository products;
    private final CustomerOrderRepository orders;
    private final ApiMapper mapper;

    public OrderService(CustomerRepository customers, CartRepository carts, ProductRepository products,
                        CustomerOrderRepository orders, ApiMapper mapper) {
        this.customers = customers;
        this.carts = carts;
        this.products = products;
        this.orders = orders;
        this.mapper = mapper;
    }

    @Transactional
    public OrderDto checkout(CheckoutRequestDto request) {
        AuthContext.requireSelf(request.getCustomerId());
        Customer customer = customers.findById(request.getCustomerId())
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy khách hàng"));
        Cart cart = carts.findByCustomerId(customer.getId()).orElse(null);
        if (cart == null || cart.getItems().isEmpty()) {
            throw ApiException.badRequest("ERR_CART_EMPTY", "Giỏ hàng trống");
        }

        List<CartItem> lines = cart.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                .toList();

        List<String> shortages = new ArrayList<>();
        List<ReservedLine> reserved = new ArrayList<>();
        for (CartItem line : lines) {
            Product product = products.findByIdForUpdate(line.getProduct().getId())
                    .orElseThrow(() -> ApiException.notFound("Không tìm thấy sản phẩm"));
            if (product.getStockQuantity() < line.getQuantity()) {
                shortages.add(product.getName() + " chỉ còn " + product.getStockQuantity());
            }
            reserved.add(new ReservedLine(line.getQuantity(), product));
        }
        if (!shortages.isEmpty()) {
            throw ApiException.badRequest("ERR_STOCK_EMPTY", "Sản phẩm đã hết hàng", shortages);
        }

        CustomerOrder order = new CustomerOrder();
        order.setCode(nextCode());
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setNote(Texts.blankToNull(request.getNote()));
        order.setCreatedAt(Instant.now());

        BigDecimal total = BigDecimal.ZERO;
        for (ReservedLine line : reserved) {
            Product product = line.product();
            product.setStockQuantity(product.getStockQuantity() - line.quantity());
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(line.quantity());
            order.addItem(item);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(line.quantity())));
        }
        order.setTotalAmount(total);
        CustomerOrder saved = orders.save(order);
        cart.getItems().clear();
        carts.save(cart);
        return mapper.toOrder(saved);
    }

    public OrderPageDto list(OrderStatusDto status, Integer page, Integer size) {
        AuthContext.requireAdmin();
        var pageable = Pages.of(page, size);
        var result = status == null
                ? orders.searchAll(pageable)
                : orders.searchByStatus(OrderStatus.valueOf(status.name()), pageable);
        return mapper.toOrderPage(result);
    }

    public OrderDto get(Long orderId) {
        CustomerOrder order = require(orderId);
        AuthContext.requireSelf(order.getCustomer().getId());
        return mapper.toOrder(order);
    }

    @Transactional
    public OrderDto updateStatus(Long orderId, OrderStatusUpdateDto request) {
        AuthContext.requireAdmin();
        CustomerOrder order = require(orderId);
        OrderStatus target = OrderStatus.valueOf(request.getStatus().name());
        if (!allowed(order.getStatus(), target)) {
            throw ApiException.badRequest("ERR_INVALID_STATUS",
                    "Không thể chuyển từ " + order.getStatus() + " sang " + target);
        }
        if (target == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                Product product = products.findByIdForUpdate(item.getProduct().getId())
                        .orElseThrow(() -> ApiException.notFound("Không tìm thấy sản phẩm để hoàn kho"));
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            }
        }
        order.setStatus(target);
        return mapper.toOrder(order);
    }

    private boolean allowed(OrderStatus from, OrderStatus to) {
        return switch (from) {
            case PENDING -> to == OrderStatus.PAID || to == OrderStatus.CANCELLED;
            case PAID -> to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED, CANCELLED -> false;
        };
    }

    private CustomerOrder require(Long orderId) {
        return orders.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy đơn hàng"));
    }

    private String nextCode() {
        String code;
        do {
            int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
            code = "DH" + LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + suffix;
        } while (orders.existsByCode(code));
        return code;
    }

    private record ReservedLine(int quantity, Product product) {
    }
}
