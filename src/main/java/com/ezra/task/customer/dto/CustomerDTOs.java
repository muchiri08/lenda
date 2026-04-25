package com.ezra.task.customer.dto;

import com.ezra.task.customer.entity.Customer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public interface CustomerDTOs {
    record CustomerRequest(@NotBlank String fullName, @NotBlank @Email String email, @NotBlank String phone) {
        public Customer toCustomer() {
            return new Customer(fullName, email, phone);
        }
    }

    record CustomerResponse(Integer id, String fullName, String email, String phone, Integer creditScore) {
        public static CustomerResponse fromCustomer(Customer customer) {
            return new CustomerResponse(customer.getId(), customer.getFullName(), customer.getEmail(), customer.getPhone(), customer.getCreditScore());
        }
    }
}
