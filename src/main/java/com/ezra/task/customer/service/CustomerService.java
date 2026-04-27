package com.ezra.task.customer.service;

import com.ezra.task.customer.dto.CustomerDTOs.*;
import com.ezra.task.customer.entity.Customer;
import com.ezra.task.customer.repository.CustomerRepository;
import com.ezra.task.exception.ResourceNotFoundException;
import com.ezra.task.loan.service.LoanLimitPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;


    @Transactional
    public Customer createCustomer(CustomerRequest request) {
        var customer = request.toCustomer();
        return customerRepository.save(customer);
    }

    public CustomerResponse getCustomerById(Integer id) {
        var customer = findCustomerById(id).orElseThrow(
                () -> new ResourceNotFoundException("Customer not found")
        );
        return CustomerResponse.fromCustomer(customer);
    }

    public LoanLimitResponse getCustomerLoanLimit(Integer id) {
        var customer = findCustomerById(id).orElseThrow(
                () -> new ResourceNotFoundException("Customer not found")
        );
        var creditScore = customer.getCreditScore();
        return new LoanLimitResponse(creditScore, LoanLimitPolicy.resolveLimit(creditScore));
    }

    private Optional<Customer> findCustomerById(Integer id) {
        return customerRepository.findById(id);
    }
}
