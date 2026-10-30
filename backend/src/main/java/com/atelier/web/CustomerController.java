package com.atelier.web;

import com.atelier.api.CustomersApi;
import com.atelier.api.model.CustomerDto;
import com.atelier.api.model.CustomerRegisterDto;
import com.atelier.api.model.CustomerUpdateDto;
import com.atelier.api.model.OrderPageDto;
import com.atelier.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerController implements CustomersApi {

    private final CustomerService customers;

    public CustomerController(CustomerService customers) {
        this.customers = customers;
    }

    @Override
    public ResponseEntity<CustomerDto> registerCustomer(CustomerRegisterDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customers.register(request));
    }

    @Override
    public ResponseEntity<CustomerDto> getCustomer(Long customerId) {
        return ResponseEntity.ok(customers.get(customerId));
    }

    @Override
    public ResponseEntity<CustomerDto> updateCustomer(Long customerId, CustomerUpdateDto request) {
        return ResponseEntity.ok(customers.update(customerId, request));
    }

    @Override
    public ResponseEntity<OrderPageDto> listCustomerOrders(Long customerId, Integer page, Integer size) {
        return ResponseEntity.ok(customers.listOrders(customerId, page, size));
    }
}
