package com.ezra.task.loan.repository;

import com.ezra.task.loan.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
}
