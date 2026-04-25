package com.ezra.task.loan.repository;

import com.ezra.task.loan.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Integer> {
    @Query("SELECT l FROM Loan l LEFT JOIN FETCH l.installments LEFT JOIN FETCH l.fees WHERE l.status IN ('OPEN', 'OVERDUE')")
    List<Loan> findActiveLoans();
}
