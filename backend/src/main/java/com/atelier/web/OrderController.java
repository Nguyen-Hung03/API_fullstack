package com.atelier.web;

import com.atelier.api.OrdersApi;
import com.atelier.api.model.CheckoutRequestDto;
import com.atelier.api.model.OrderDto;
import com.atelier.api.model.OrderPageDto;
import com.atelier.api.model.OrderStatusDto;
import com.atelier.api.model.OrderStatusUpdateDto;
import com.atelier.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController implements OrdersApi {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @Override
    public ResponseEntity<OrderDto> checkout(CheckoutRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orders.checkout(request));
    }

    @Override
    public ResponseEntity<OrderPageDto> listOrders(OrderStatusDto status, Integer page, Integer size) {
        return ResponseEntity.ok(orders.list(status, page, size));
    }

    @Override
    public ResponseEntity<OrderDto> getOrder(Long orderId) {
        return ResponseEntity.ok(orders.get(orderId));
    }

    @Override
    public ResponseEntity<OrderDto> updateOrderStatus(Long orderId, OrderStatusUpdateDto request) {
        return ResponseEntity.ok(orders.updateStatus(orderId, request));
    }
}
