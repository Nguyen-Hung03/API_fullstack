package com.atelier.service;

import com.atelier.api.model.CartDto;
import com.atelier.api.model.CartItemUpdateDto;
import com.atelier.api.model.CartItemWriteDto;
import com.atelier.domain.Cart;
import com.atelier.domain.CartItem;
import com.atelier.domain.Customer;
import com.atelier.domain.Product;
import com.atelier.error.ApiException;
import com.atelier.repo.CartRepository;
import com.atelier.repo.CustomerRepository;
import com.atelier.repo.ProductRepository;
import com.atelier.security.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository carts;
    private final CustomerRepository customers;
    private final ProductRepository products;
    private final ApiMapper mapper;

    public CartService(CartRepository carts, CustomerRepository customers, ProductRepository products, ApiMapper mapper) {
        this.carts = carts;
        this.customers = customers;
        this.products = products;
        this.mapper = mapper;
    }

    public CartDto get(Long customerId) {
        AuthContext.requireSelf(customerId);
        requireCustomer(customerId);
        return carts.findByCustomerId(customerId)
                .map(mapper::toCart)
                .orElseGet(() -> mapper.emptyCart(customerId));
    }

    @Transactional
    public CartDto add(Long customerId, CartItemWriteDto request) {
        AuthContext.requireSelf(customerId);
        Customer customer = requireCustomer(customerId);
        Product product = products.findById(request.getProductId())
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy sản phẩm"));
        Cart cart = carts.findByCustomerId(customerId).orElseGet(() -> newCart(customer));
        CartItem line = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);
        int next = request.getQuantity();
        if (line != null) {
            next = line.getQuantity() + request.getQuantity();
        }
        if (next > 99) {
            throw ApiException.badRequest("ERR_QUANTITY", "Số lượng tối đa mỗi sản phẩm là 99");
        }
        if (line == null) {
            line = new CartItem();
            line.setCart(cart);
            line.setProduct(product);
            line.setQuantity(next);
            cart.getItems().add(line);
        } else {
            line.setQuantity(next);
        }
        carts.saveAndFlush(cart);
        return mapper.toCart(carts.findByCustomerId(customerId).orElseThrow());
    }

    @Transactional
    public CartDto update(Long customerId, Long itemId, CartItemUpdateDto request) {
        AuthContext.requireSelf(customerId);
        requireCustomer(customerId);
        Cart cart = requireCart(customerId);
        CartItem line = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy dòng giỏ hàng"));
        line.setQuantity(request.getQuantity());
        carts.saveAndFlush(cart);
        return mapper.toCart(carts.findByCustomerId(customerId).orElseThrow());
    }

    @Transactional
    public void remove(Long customerId, Long itemId) {
        AuthContext.requireSelf(customerId);
        requireCustomer(customerId);
        Cart cart = requireCart(customerId);
        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw ApiException.notFound("Không tìm thấy dòng giỏ hàng");
        }
        carts.save(cart);
    }

    private Cart newCart(Customer customer) {
        Cart cart = new Cart();
        cart.setCustomer(customer);
        return carts.save(cart);
    }

    private Cart requireCart(Long customerId) {
        return carts.findByCustomerId(customerId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy giỏ hàng"));
    }

    private Customer requireCustomer(Long customerId) {
        return customers.findById(customerId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy khách hàng"));
    }
}
