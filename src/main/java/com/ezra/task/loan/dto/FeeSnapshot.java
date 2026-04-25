package com.ezra.task.loan.dto;

import com.ezra.task.common.FeeCalculationType;
import com.ezra.task.common.FeeType;

import java.math.BigDecimal;

public record FeeSnapshot(FeeType feeType, FeeCalculationType calculationType, Boolean appliedAtOrigination, BigDecimal calculatedAmount, Float percentage) {
}
