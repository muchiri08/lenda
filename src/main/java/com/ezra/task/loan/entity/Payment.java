package com.ezra.task.loan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate paymentDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    private List<PaymentAllocation> allocations = new ArrayList<>();

    @CreationTimestamp
    private Instant createdAt;

    public Payment(BigDecimal amount, PaymentMethod method) {
        this.amount = amount;
        this.method = method;
    }

    public enum PaymentMethod {
        CASH, BANK_TRANSFER, MOBILE_MONEY, CARD, OTHER
    }
}
