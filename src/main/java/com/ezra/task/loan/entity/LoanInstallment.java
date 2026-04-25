package com.ezra.task.loan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loan_installments")
@Getter
@Setter
@NoArgsConstructor
public class LoanInstallment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @Column(nullable = false)
    private LocalDate dueDate;

    private BigDecimal amountDue;

    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private LocalDate paidDate;

    private boolean lateFeeApplied = false;

    private BigDecimal lateFeeAmount = BigDecimal.ZERO;

    public LoanInstallment(Loan loan, LocalDate dueDate, BigDecimal amountDue) {
        this.loan = loan;
        this.dueDate = dueDate;
        this.amountDue = amountDue != null ? amountDue : BigDecimal.ZERO;
    }

    public boolean isPaid() {
        return status == Status.PAID;
    }

    public BigDecimal remaining() {
        return amountDue.subtract(amountPaid);
    }

    public void applyPayment(BigDecimal payment) {
        if (isPaid()) return;
        var remaining = remaining();
        if (payment.compareTo(remaining) >= 0) {
            this.amountPaid = amountDue;
            this.status = Status.PAID;
        } else {
            this.amountPaid = this.amountPaid.add(payment);
            this.status = Status.PARTIALLY_PAID;
        }
    }

    public void applyLateFeeIfApplicable(LocalDate today, Integer gracePeriod, BigDecimal lateFee) {
        if (isPaid()) return;
        if (!today.isAfter(dueDate)) return;
        long daysOverdue = ChronoUnit.DAYS.between(dueDate, today);
        if (daysOverdue >= gracePeriod && !lateFeeApplied) {
            this.lateFeeAmount = this.lateFeeAmount.add(lateFee);
            this.lateFeeApplied = true;
            this.status = Status.OVERDUE;
        }
    }

    public enum Status {
        PENDING,
        PARTIALLY_PAID,
        PAID,
        OVERDUE,
    }
}
