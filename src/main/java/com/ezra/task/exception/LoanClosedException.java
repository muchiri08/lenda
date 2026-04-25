package com.ezra.task.exception;

public class LoanClosedException extends RuntimeException {
    public LoanClosedException(String message) {
        super(message);
    }
}
