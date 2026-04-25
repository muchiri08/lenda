package com.ezra.task.loan.dto;

import com.ezra.task.loan.entity.LoanInstallment;

import java.math.BigDecimal;

public record AllocationResult(LoanInstallment installment, BigDecimal amount) {
}
