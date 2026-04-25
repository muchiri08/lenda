package com.ezra.task.customer.controller;

import com.ezra.task.customer.dto.CustomerDTOs.*;
import com.ezra.task.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("api/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<Void> createCustomer(@RequestBody @Valid CustomerRequest request, UriComponentsBuilder uriBuilder) {
        var customer = customerService.createCustomer(request);
        var location = uriBuilder.path("/api/customers/{id}")
                .buildAndExpand(customer.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable Integer id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }
}
