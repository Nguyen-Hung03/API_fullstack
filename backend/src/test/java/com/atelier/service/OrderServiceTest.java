package com.atelier.service;

import com.atelier.api.model.CheckoutRequestDto;
import com.atelier.api.model.OrderStatusDto;
import com.atelier.domain.Cart;
import com.atelier.domain.CartItem;
import com.atelier.domain.Category;
import com.atelier.domain.Customer;
import com.atelier.domain.CustomerOrder;
import com.atelier.domain.Product;
import com.atelier.error.ApiException;
import com.atelier.repo.CartRepository;
import com.atelier.repo.CustomerOrderRepository;
import com.atelier.repo.CustomerRepository;
import com.atelier.repo.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CustomerRepository customers;
    @Mock
    private CartRepository carts;
    @Mock
    private ProductRepository products;
    @Mock
    private CustomerOrderRepository orders;

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(customers, carts, products, orders, new ApiMapper());
    }

    @Test
    void checkoutCreatesPendingOrderAndDeductsStock() {
        Customer customer = customer();
        Product product = product(5);
        Cart cart = cart(customer, product, 2);

        when(customers.findById(1L)).thenReturn(Optional.of(customer));
        when(carts.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(products.findByIdForUpdate(10L)).thenReturn(Optional.of(product));
        when(orders.existsByCode(anyString())).thenReturn(false);
        when(orders.save(any(CustomerOrder.class))).thenAnswer(invocation -> {
            CustomerOrder saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        CheckoutRequestDto request = new CheckoutRequestDto();
        request.setCustomerId(1L);
        var result = service.checkout(request);

        assertEquals(OrderStatusDto.PENDING, result.getStatus());
        assertEquals(0, new BigDecimal("200000").compareTo(result.getTotalAmount()));
        assertEquals(3, product.getStockQuantity());
        assertTrue(cart.getItems().isEmpty());
        assertTrue(result.getCode().startsWith("DH"));
        assertEquals(1, result.getItems().size());
    }

    @Test
    void checkoutFailsWhenStockIsNotEnoughAndDoesNotCreateOrder() {
        Customer customer = customer();
        Product product = product(1);
        Cart cart = cart(customer, product, 2);

        when(customers.findById(1L)).thenReturn(Optional.of(customer));
        when(carts.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(products.findByIdForUpdate(10L)).thenReturn(Optional.of(product));

        CheckoutRequestDto request = new CheckoutRequestDto();
        request.setCustomerId(1L);
        ApiException ex = assertThrows(ApiException.class, () -> service.checkout(request));

        assertEquals("ERR_STOCK_EMPTY", ex.getCode());
        assertEquals(1, product.getStockQuantity());
        assertEquals(1, cart.getItems().size());
        verify(orders, never()).save(any());
    }

    private Customer customer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFullName("Nguyễn Thị Lan");
        return customer;
    }

    private Product product(int stock) {
        Category category = new Category();
        category.setId(3L);
        category.setName("Gốm sứ");
        Product product = new Product();
        product.setId(10L);
        product.setName("Bình gốm men rạn");
        product.setPrice(new BigDecimal("100000"));
        product.setStockQuantity(stock);
        product.setCategory(category);
        return product;
    }

    private Cart cart(Customer customer, Product product, int quantity) {
        CartItem item = new CartItem();
        item.setId(7L);
        item.setProduct(product);
        item.setQuantity(quantity);
        Cart cart = new Cart();
        cart.setCustomer(customer);
        cart.setItems(new ArrayList<>(List.of(item)));
        item.setCart(cart);
        return cart;
    }
}
