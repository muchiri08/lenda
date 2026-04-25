package com.ezra.task.loan.entity;

import com.ezra.task.common.FeeCalculationType;
import com.ezra.task.common.FeeType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "loan_fees")
@Getter
@Setter
public class LoanFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @Enumerated(EnumType.STRING)
    private FeeType type;

    @Enumerated(EnumType.STRING)
    private FeeCalculationType calculationType;

    private BigDecimal amount;

    private Boolean appliedAtOrigination;

    private Float percentage;

    @CreationTimestamp
    private Instant createdAt;
}
