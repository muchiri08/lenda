package com.ezra.task.loan.service;

import com.ezra.task.common.FeeCalculationType;
import com.ezra.task.customer.entity.Customer;
import com.ezra.task.customer.repository.CustomerRepository;
import com.ezra.task.exception.EntityNotFoundException;
import com.ezra.task.exception.LoanClosedException;
import com.ezra.task.exception.LoanLimitExceedsException;
import com.ezra.task.exception.ResourceNotFoundException;
import com.ezra.task.loan.LoanCreatedEvent;
import com.ezra.task.loan.LoanOverdueEvent;
import com.ezra.task.loan.PaymentEvent;
import com.ezra.task.loan.dto.LoanDTOs.*;
import com.ezra.task.loan.dto.FeeSnapshot;
import com.ezra.task.loan.entity.Loan;
import com.ezra.task.loan.entity.PaymentAllocation;
import com.ezra.task.loan.repository.LoanRepository;
import com.ezra.task.loan.repository.PaymentRepository;
import com.ezra.task.product.entity.Product;
import com.ezra.task.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanService {
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final LoanRepository loanRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PaymentRepository paymentRepository;

    @Transactional
    public void makePayment(Integer loanId, PaymentRequest request) {
        var loan = findLoanById(loanId).orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        if (loan.getStatus() == Loan.Status.CLOSED) {
            throw new LoanClosedException("The loan is already settled.");
        }
        var payment = request.toPayment();
        payment.setLoan(loan);
        var savedPayment = paymentRepository.save(payment);
        var allocations = loan.applyPayment(payment.getAmount(), payment.getPaymentDate());
        for (var alloc : allocations) {
            var paymentAllocation = new PaymentAllocation();
            paymentAllocation.setPayment(payment);
            paymentAllocation.setInstallment(alloc.installment());
            paymentAllocation.setAmount(alloc.amount());
            savedPayment.getAllocations().add(paymentAllocation);
        }

        applicationEventPublisher.publishEvent(new PaymentEvent(loan.getId(), payment.getAmount()));
    }

    @Transactional
    public Loan applyLoan(LoanRequest request) {
        var customer = getCustomerById(request.customerId());
        validateAmountBorrowed(customer.getCreditScore(), request.amount());
        var product = getProductById(request.productId());

        var feeSnapshots = product.getFees()
                .stream()
                .map(fee -> new FeeSnapshot(
                        fee.getType(),
                        fee.getCalculationType(),
                        fee.getApplyAtOrigination(),
                        fee.calculate(request.amount()),
                        fee.getCalculationType() == FeeCalculationType.PERCENTAGE ? fee.getAmount().floatValue() : null
                ))
                .toList();

        var loan = Loan.create(
                request.customerId(),
                request.productId(),
                product.getTenureType(),
                product.getTenureValue(),
                request.amount(),
                request.type(),
                product.getGracePeriod(),
                feeSnapshots
        );

        var savedLoan = loanRepository.save(loan);

        applicationEventPublisher.publishEvent(new LoanCreatedEvent(loan.getId(), customer.getFullName()));

        return savedLoan;
    }

    public LoanResponse getLoanById(Integer id) {
        var loan = findLoanById(id).orElseThrow(
                () -> new ResourceNotFoundException("Loan not found")
        );
        var customer = getCustomerById(loan.getCustomerId());
        return LoanResponse.fromLoanDetailed(loan, customer);
    }

    public List<LoanResponse> getAllLoans() {
        return loanRepository.findAll().stream().map(LoanResponse::fromLoanAll).toList();
    }

    @Transactional
    public void processOverdueLoans() {
        var today = LocalDate.now();
        var loans = loanRepository.findActiveLoans();
        for (var loan : loans) {
            loan.handleOverdue(today);

            if (loan.getStatus() == Loan.Status.OVERDUE) {
                applicationEventPublisher.publishEvent(new LoanOverdueEvent(loan.getId()));
            }
        }
    }

    private Optional<Loan> findLoanById(Integer id) {
        return loanRepository.findById(id);
    }

    private Customer getCustomerById(Integer id) {
        return customerRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Customer not found")
        );
    }

    private Product getProductById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }

    private void validateAmountBorrowed(Integer creditScore, BigDecimal amount) {
        var limit = LoanLimitPolicy.resolveLimit(creditScore);
        if (amount.compareTo(limit) > 0) {
            throw new LoanLimitExceedsException("Requested amount exceeds your loan limit. Your current limit is " + limit);
        }
    }
}
