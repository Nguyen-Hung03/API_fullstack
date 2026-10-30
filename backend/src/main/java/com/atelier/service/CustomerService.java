package com.atelier.service;

import com.atelier.api.model.CustomerDto;
import com.atelier.api.model.CustomerRegisterDto;
import com.atelier.api.model.CustomerUpdateDto;
import com.atelier.api.model.OrderPageDto;
import com.atelier.domain.Cart;
import com.atelier.domain.Customer;
import com.atelier.domain.Role;
import com.atelier.error.ApiException;
import com.atelier.repo.CartRepository;
import com.atelier.repo.CustomerOrderRepository;
import com.atelier.repo.CustomerRepository;
import com.atelier.security.AuthContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customers;
    private final CartRepository carts;
    private final CustomerOrderRepository orders;
    private final PasswordEncoder passwordEncoder;
    private final ApiMapper mapper;

    public CustomerService(CustomerRepository customers, CartRepository carts, CustomerOrderRepository orders,
                           PasswordEncoder passwordEncoder, ApiMapper mapper) {
        this.customers = customers;
        this.carts = carts;
        this.orders = orders;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    @Transactional
    public CustomerDto register(CustomerRegisterDto request) {
        String email = request.getEmail().trim().toLowerCase();
        if (customers.existsByEmailIgnoreCase(email)) {
            throw ApiException.unprocessable("ERR_DUPLICATE_EMAIL", "Email đã được đăng ký",
                    List.of("email: Email đã được đăng ký"));
        }
        Customer customer = new Customer();
        customer.setFullName(request.getFullName().trim());
        customer.setEmail(email);
        customer.setPhone(request.getPhone().trim());
        customer.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        customer.setAddress(Texts.blankToNull(request.getAddress()));
        customer.setRole(Role.CUSTOMER);
        customer.setCreatedAt(Instant.now());
        customer = customers.save(customer);

        Cart cart = new Cart();
        cart.setCustomer(customer);
        carts.save(cart);
        return mapper.toCustomer(customer);
    }

    public CustomerDto get(Long customerId) {
        AuthContext.requireSelf(customerId);
        return mapper.toCustomer(requireCustomer(customerId));
    }

    @Transactional
    public CustomerDto update(Long customerId, CustomerUpdateDto request) {
        AuthContext.requireSelf(customerId);
        Customer customer = requireCustomer(customerId);
        customer.setFullName(request.getFullName().trim());
        customer.setPhone(request.getPhone().trim());
        customer.setAddress(Texts.blankToNull(request.getAddress()));
        return mapper.toCustomer(customer);
    }

    public OrderPageDto listOrders(Long customerId, Integer page, Integer size) {
        AuthContext.requireSelf(customerId);
        requireCustomer(customerId);
        return mapper.toOrderPage(orders.searchByCustomer(customerId, Pages.of(page, size)));
    }

    private Customer requireCustomer(Long customerId) {
        return customers.findById(customerId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy khách hàng"));
    }
}
