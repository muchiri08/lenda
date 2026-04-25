package com.ezra.task.notification;

import com.ezra.task.loan.LoanCreatedEvent;
import com.ezra.task.loan.LoanOverdueEvent;
import com.ezra.task.loan.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    // It works with console notification by default because it is the only implementor
    // If we had other impl, we would possibly have a strategy/factory pattern to pick which impl the user needs
    private final NotificationChannel notificationChannel;

    @Async
    @EventListener
    public void onLoanCreated(LoanCreatedEvent event) {
        notificationChannel.send("Loan created: loanId=" + event.loanId() + ", borrower=" + event.customerName());
    }

    @Async
    @EventListener
    public void onPayment(PaymentEvent event) {
        notificationChannel.send("Payment received: loanId=" + event.loanId() + ", amount=" + event.amount());
    }

    @Async
    @EventListener
    public void onOverdue(LoanOverdueEvent event) {
        notificationChannel.send("Loan Overdue: loanId=" + event.loanId());
    }
}
