package com.atelier.web;

import com.atelier.api.CartApi;
import com.atelier.api.model.CartDto;
import com.atelier.api.model.CartItemUpdateDto;
import com.atelier.api.model.CartItemWriteDto;
import com.atelier.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CartController implements CartApi {

    private final CartService carts;

    public CartController(CartService carts) {
        this.carts = carts;
    }

    @Override
    public ResponseEntity<CartDto> getCart(Long customerId) {
        return ResponseEntity.ok(carts.get(customerId));
    }

    @Override
    public ResponseEntity<CartDto> addCartItem(Long customerId, CartItemWriteDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carts.add(customerId, request));
    }

    @Override
    public ResponseEntity<CartDto> updateCartItem(Long customerId, Long itemId, CartItemUpdateDto request) {
        return ResponseEntity.ok(carts.update(customerId, itemId, request));
    }

    @Override
    public ResponseEntity<Void> removeCartItem(Long customerId, Long itemId) {
        carts.remove(customerId, itemId);
        return ResponseEntity.noContent().build();
    }
}
