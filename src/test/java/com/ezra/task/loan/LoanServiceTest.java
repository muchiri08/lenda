package com.ezra.task.loan;

import com.ezra.task.common.TenureType;
import com.ezra.task.customer.entity.Customer;
import com.ezra.task.customer.repository.CustomerRepository;
import com.ezra.task.exception.EntityNotFoundException;
import com.ezra.task.exception.LoanLimitExceedsException;
import com.ezra.task.loan.dto.LoanDTOs;
import com.ezra.task.loan.entity.Loan;
import com.ezra.task.loan.entity.Payment;
import com.ezra.task.loan.repository.LoanRepository;
import com.ezra.task.loan.repository.PaymentRepository;
import com.ezra.task.loan.service.LoanService;
import com.ezra.task.product.entity.Product;
import com.ezra.task.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LoanService loanService;

    @Test
    void should_create_loan_successfully() {
        var request = new LoanDTOs.LoanRequest(1, 1, BigDecimal.valueOf(1000), Loan.Type.INSTALLMENT);

        var customer = new Customer();
        customer.setId(1);
        customer.setFullName("John Doe");
        customer.setCreditScore(20);

        var product = new Product();
        product.setId(1);
        product.setTenureType(TenureType.MONTHS);
        product.setTenureValue(3);
        product.setGracePeriod(0);
        product.setFees(List.of()); // just keeping it simple

        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(loanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var loan = loanService.applyLoan(request);

        assertNotNull(loan);
        assertEquals(3, loan.getInstallments().size());

        verify(loanRepository).save(any());
        verify(eventPublisher).publishEvent(any(LoanCreatedEvent.class));
    }

    @Test
    void should_reject_loan_when_above_limit() {
        Customer customer = new Customer();
        customer.setId(1);
        customer.setCreditScore(20); // limit = 30_000

        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));

        var request = new LoanDTOs.LoanRequest(1, 1, BigDecimal.valueOf(40_000), Loan.Type.LUMP_SUM);

        assertThrows(LoanLimitExceedsException.class,
                () -> loanService.applyLoan(request));
    }

    @Test
    void should_throw_when_customer_not_found() {
        var request = new LoanDTOs.LoanRequest(1, 1, BigDecimal.valueOf(1000), Loan.Type.LUMP_SUM);

        when(customerRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> loanService.applyLoan(request));
    }

    @Test
    void should_allocate_payment_across_installments() {
        var loan = TestData.loanWithInstallments(3, 100); // helper

        when(loanRepository.findById(1)).thenReturn(Optional.of(loan));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new LoanDTOs.PaymentRequest(BigDecimal.valueOf(250), Payment.PaymentMethod.MOBILE_MONEY);

        loanService.makePayment(1, request);

        var installments = loan.getInstallments();

        assertTrue(installments.get(0).isPaid());
        assertTrue(installments.get(1).isPaid());
        assertFalse(installments.get(2).isPaid());

        verify(eventPublisher).publishEvent(any(PaymentEvent.class));
    }

    @Test
    void should_mark_loan_as_overdue() {
        var loan = TestData.loanWithInstallments(1, 100);
        // make installment overdue
        loan.getInstallments().get(0)
                .setDueDate(LocalDate.now().minusDays(5));

        when(loanRepository.findActiveLoans()).thenReturn(List.of(loan));

        loanService.processOverdueLoans();

        assertEquals(Loan.Status.OVERDUE, loan.getStatus());

        verify(eventPublisher).publishEvent(any(LoanOverdueEvent.class));
    }
}
