package com.ezra.task.loan;

import com.ezra.task.loan.entity.Loan;
import com.ezra.task.loan.entity.LoanInstallment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

public class TestData {
    static Loan loanWithInstallments(int count, int amountPerInstallment) {
        var loan = new Loan();
        loan.setOutstandingBalance(BigDecimal.valueOf((long) count * amountPerInstallment));
        loan.setGracePeriod(3);

        var list = new ArrayList<LoanInstallment>();
        for (int i = 1; i <= count; i++) {
            list.add(new LoanInstallment(
                    loan,
                    LocalDate.now().plusMonths(i),
                    BigDecimal.valueOf(amountPerInstallment)
            ));
        }

        loan.setInstallments(list);
        return loan;
    }
}
