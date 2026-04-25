package com.ezra.task.loan.controller;

import com.ezra.task.loan.dto.LoanDTOs.*;
import com.ezra.task.loan.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("api/loans")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;

    @PostMapping("payments/{loanId}")
    public ResponseEntity<Void> makePayment(@PathVariable Integer loanId, @RequestBody @Valid PaymentRequest request) {
        loanService.makePayment(loanId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<Void> applyLoan(@RequestBody @Valid LoanRequest request, UriComponentsBuilder uriBuilder) {
        var loan = loanService.applyLoan(request);
        var location = uriBuilder.path("/api/loans/{id}")
                .buildAndExpand(loan.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public ResponseEntity<List<LoanResponse>> getLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @GetMapping("{id}")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable Integer id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }
}
