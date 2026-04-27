package com.ezra.task.loan.service;

import java.math.BigDecimal;

// Just for demo purposes, it tries to mimic loan awards based on credit score
public interface LoanLimitPolicy {
    static BigDecimal resolveLimit(Integer creditScore) {
        return switch (creditScore) {
            case Integer score when score <= 10 -> BigDecimal.ZERO;
            case Integer score when score <= 30 -> BigDecimal.valueOf(10_000);
            case Integer score when score <= 50 -> BigDecimal.valueOf(20_000);
            case Integer score when score <= 70 -> BigDecimal.valueOf(30_000);
            case Integer score when score <= 90 -> BigDecimal.valueOf(40_000);
            default -> BigDecimal.valueOf(50_000);
        };
    }
}
