package com.ezra.task.exception;

public class LoanLimitExceedsException extends RuntimeException {
    public LoanLimitExceedsException(String message) {
        super(message);
    }
}
