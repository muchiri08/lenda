package com.ezra.task.customer.repository;

import com.ezra.task.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}
