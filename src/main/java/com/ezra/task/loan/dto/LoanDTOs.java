package com.ezra.task.loan.dto;

import com.ezra.task.common.FeeCalculationType;
import com.ezra.task.common.FeeType;
import com.ezra.task.common.TenureType;
import com.ezra.task.customer.entity.Customer;
import com.ezra.task.loan.entity.Loan;
import com.ezra.task.loan.entity.LoanFee;
import com.ezra.task.loan.entity.LoanInstallment;
import com.ezra.task.loan.entity.Payment;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LoanDTOs {
    record LoanRequest(
            Integer customerId,
            Integer productId,
            BigDecimal amount,
            Loan.Type type
    ) {
    }

    record LoanResponse(
            Integer id,
            Loan.Type type,
            TenureType tenureType,
            Integer tenureValue,
            BigDecimal principalAmount,
            BigDecimal outstandingBalance,
            BigDecimal totalFeesApplied,
            BigDecimal disbursedAmount,
            Loan.Status status,
            LocalDate dueDate,
            @JsonInclude(JsonInclude.Include.NON_NULL) Borrower borrower,
            @JsonInclude(JsonInclude.Include.NON_NULL) List<LoanFeeResponse> fees,
            @JsonInclude(JsonInclude.Include.NON_NULL) List<LoanInstallmentResponse> installments,
            @JsonInclude(JsonInclude.Include.NON_NULL) List<PaymentResponse> payments
    ) {
        public static LoanResponse fromLoanAll(Loan loan) {
            return fromLoan(loan, null, false);
        }

        public static LoanResponse fromLoanDetailed(Loan loan, Customer customer) {
            return fromLoan(loan, customer, true);
        }

        private static LoanResponse fromLoan(Loan loan, Customer customer, boolean detailed) {
            return new LoanResponse(
                    loan.getId(),
                    loan.getType(),
                    loan.getTenureType(),
                    loan.getTenureValue(),
                    loan.getPrincipalAmount(),
                    loan.getOutstandingBalance(),
                    loan.getTotalFeesApplied(),
                    loan.getDisbursedAmount(),
                    loan.getStatus(),
                    loan.getDueDate(),
                    detailed ? Borrower.fromCustomer(customer) : null,
                    detailed ? LoanFeeResponse.fromLoanFees(loan.getFees()) : null,
                    detailed ? LoanInstallmentResponse.fromLoanInstallments(loan.getInstallments()) : null,
                    detailed ? PaymentResponse.fromPayments(loan.getPayments()) : null
            );
        }

    }

    record Borrower(
            Integer id,
            String fullName,
            Integer creditScore
    ) {
        public static Borrower fromCustomer(Customer customer) {
            return new Borrower(customer.getId(), customer.getFullName(), customer.getCreditScore());
        }
    }

    record LoanFeeResponse(
            Integer id,
            FeeType type,
            FeeCalculationType calculationType,
            Float percentage,
            Boolean appliedAtOrigination,
            BigDecimal amount
    ) {
        public static List<LoanFeeResponse> fromLoanFees(List<LoanFee> fees) {
            return fees.stream()
                    .map(fee -> new LoanFeeResponse(fee.getId(), fee.getType(), fee.getCalculationType(), fee.getPercentage(), fee.getAppliedAtOrigination(), fee.getAmount()))
                    .toList();
        }
    }

    record LoanInstallmentResponse(
            Integer id,
            LocalDate dueDate,
            BigDecimal amountDue,
            BigDecimal amountPaid,
            LoanInstallment.Status status
    ) {
        public static List<LoanInstallmentResponse> fromLoanInstallments(List<LoanInstallment> installments) {
            return installments.stream()
                    .map(i -> new LoanInstallmentResponse(i.getId(), i.getDueDate(), i.getAmountDue(), i.getAmountPaid(), i.getStatus()))
                    .toList();
        }
    }

    record PaymentRequest(
            @NotNull @Positive BigDecimal amount,
            @NotNull Payment.PaymentMethod method
    ) {
        public Payment toPayment() {
            return new Payment(amount, method);
        }
    }

    record PaymentResponse(
            Integer id,
            BigDecimal amount,
            Payment.PaymentMethod method,
            LocalDate paymentDate
    ) {
        public static List<PaymentResponse> fromPayments(List<Payment> payments) {
            return payments.stream()
                    .map(p -> new PaymentResponse(p.getId(), p.getAmount(), p.getMethod(), p.getPaymentDate()))
                    .toList();
        }
    }
}
