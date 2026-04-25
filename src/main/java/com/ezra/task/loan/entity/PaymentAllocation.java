package com.ezra.task.loan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_allocations")
@Getter
@Setter
public class PaymentAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_installment_id")
    private LoanInstallment installment;

    private BigDecimal amount;
}
