package com.electricity.billing.exception;

/** Thrown when a requested entity (customer, consumer, bill, complaint, etc.) cannot be found. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
