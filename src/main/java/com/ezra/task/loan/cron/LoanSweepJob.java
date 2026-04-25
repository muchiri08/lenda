package com.ezra.task.loan.cron;

import com.ezra.task.loan.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanSweepJob {
    private final LoanService loanService;

    @Scheduled(cron = "0 0 0 * * *")
    public void run() {
        loanService.processOverdueLoans();
    }
}
