package com.ezra.task.loan.entity;

import com.ezra.task.common.FeeType;
import com.ezra.task.common.TenureType;
import com.ezra.task.loan.dto.AllocationResult;
import com.ezra.task.loan.dto.FeeSnapshot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "loans")
@Getter
@Setter
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer customerId;

    private Integer productId;

    @Enumerated(EnumType.STRING)
    private TenureType tenureType;

    private Integer tenureValue;

    private BigDecimal principalAmount;

    private BigDecimal disbursedAmount;

    private BigDecimal outstandingBalance;

    private BigDecimal totalFeesApplied;

    private LocalDate originationDate;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private Type type;

    @Enumerated(EnumType.STRING)
    private Status status = Status.OPEN;

    private Integer gracePeriod;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoanFee> fees = new ArrayList<>();

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LoanInstallment> installments = new ArrayList<>();

    @OneToMany(mappedBy = "loan")
    private List<Payment> payments = new ArrayList<>();

    @CreationTimestamp
    private Instant createdAt;

    public static Loan create(Integer customerId, Integer productId, TenureType tenureType, Integer tenureValue, BigDecimal principal, Type loanType, Integer gracePeriod, List<FeeSnapshot> feeSnapshots) {
        var loan = new Loan();
        loan.customerId = customerId;
        loan.productId = productId;
        loan.principalAmount = scale(principal);
        loan.type = loanType;
        loan.originationDate = LocalDate.now();
        loan.status = Status.OPEN;
        loan.tenureType = tenureType;
        loan.tenureValue = tenureValue;
        loan.gracePeriod = gracePeriod;
        loan.dueDate = calculateDueDate(loan.originationDate, tenureType, tenureValue);

        loan.totalFeesApplied = BigDecimal.ZERO;
        loan.outstandingBalance = scale(principal);

        for (var feeSnapshot : feeSnapshots) {
            var loanFee = new LoanFee();
            loanFee.setType(feeSnapshot.feeType());
            loanFee.setAmount(feeSnapshot.calculatedAmount());
            loanFee.setCalculationType(feeSnapshot.calculationType());
            loanFee.setAppliedAtOrigination(feeSnapshot.appliedAtOrigination());
            loanFee.setPercentage(feeSnapshot.percentage());
            loan.addLoanFee(loanFee);
        }

        loan.createInstallments();

        var disbursedAmt = loan.getPrincipalAmount().subtract(loan.getTotalFeesApplied());
        loan.setDisbursedAmount(disbursedAmt);

        return loan;
    }

    public List<AllocationResult> applyPayment(BigDecimal amount, LocalDate paidDate) {
        var remaining = scale(amount);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment must be positive");
        }
        var allocations = new ArrayList<AllocationResult>();
        installments.sort(Comparator.comparing(LoanInstallment::getDueDate));
        for (var installment : installments) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            if (installment.isPaid()) continue;
            var instRemaining = installment.remaining();
            var toApply = remaining.min(instRemaining);
            installment.applyPayment(toApply);
            if (installment.isPaid()) {
                installment.setPaidDate(paidDate);
            }
            allocations.add(new AllocationResult(installment, toApply));
            remaining = remaining.subtract(toApply);
        }
        this.outstandingBalance = this.outstandingBalance.subtract(scale(amount));
        if (this.outstandingBalance.compareTo(BigDecimal.ZERO) <= 0) {
            this.status = Status.CLOSED;
        }
        return allocations;
    }

    private void addLoanFee(LoanFee fee) {
        if (fee == null) return;
        this.fees.add(fee);
        fee.setLoan(this);
        if (fee.getAppliedAtOrigination()) {
            this.totalFeesApplied = this.totalFeesApplied.add(fee.getAmount());
        }
        if (!fee.getAppliedAtOrigination() && fee.getType() == FeeType.DAILY) { // the daily interest
            this.outstandingBalance = this.outstandingBalance.add(fee.getAmount().multiply(BigDecimal.valueOf(tenureValue)));
        }
    }

    private static LocalDate calculateDueDate(LocalDate start, TenureType tenureType, Integer tenureValue) {
        return switch (tenureType) {
            case DAYS -> start.plusDays(tenureValue);
            case MONTHS -> start.plusMonths(tenureValue);
        };
    }

    private void createInstallments() {
        if (type == Type.LUMP_SUM) {
            this.installments.add(new LoanInstallment(this, dueDate, outstandingBalance));
        } else if (type == Type.INSTALLMENT) {
            var numberOfInstallments = Math.max(1, tenureValue);
            var installmentAmount = outstandingBalance.divide(BigDecimal.valueOf(numberOfInstallments), 2, RoundingMode.HALF_UP);

            for (int i = 1; i <= numberOfInstallments; i++) {
                var installmentDate = calculateInstallmentDate(i, numberOfInstallments);
                this.installments.add(new LoanInstallment(this, installmentDate, installmentAmount));
            }

            this.dueDate = installments.getLast().getDueDate();
        }
    }

    private LocalDate calculateInstallmentDate(int i, int total) {
        return switch (tenureType) {
            case DAYS -> originationDate.plusDays((long) i * tenureValue / total);
            case MONTHS -> originationDate.plusMonths(i);
        };
    }

    public void handleOverdue(LocalDate today) {
        boolean anyOverdueInstallment = false;
        for (var inst : installments) {
            if (!inst.isPaid() && today.isAfter(inst.getDueDate())) {
                anyOverdueInstallment = true;
                inst.applyLateFeeIfApplicable(today, gracePeriod, scale(getLateFee()));
            }
        }
        if (anyOverdueInstallment) {
            this.status = Status.OVERDUE;
            this.outstandingBalance = this.outstandingBalance.add(scale(getTotalLateFee()));
        }
    }

    private BigDecimal getLateFee() {
        return fees.stream().filter(f -> f.getType() == FeeType.LATE)
                .map(LoanFee::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getTotalLateFee() {
        return installments.stream().filter(i -> i.getStatus() == LoanInstallment.Status.OVERDUE)
                .map(LoanInstallment::getLateFeeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public enum Type {
        LUMP_SUM, INSTALLMENT;
    }

    public enum Status {
        OPEN, CLOSED, CANCELLED, OVERDUE, WRITTEN_OFF;
    }
}
