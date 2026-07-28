package com.electricity.billing.exception;

/** Thrown when an operation would create a record that violates a uniqueness rule. */
public class DuplicateRecordException extends RuntimeException {
    public DuplicateRecordException(String message) {
        super(message);
    }
}
