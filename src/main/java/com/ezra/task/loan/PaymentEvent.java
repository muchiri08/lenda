package com.ezra.task.loan;

import java.math.BigDecimal;

public record PaymentEvent(Integer loanId, BigDecimal amount) {
}
