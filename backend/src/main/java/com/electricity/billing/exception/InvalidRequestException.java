package com.electricity.billing.exception;

/** Thrown when a request fails a business-rule validation that Bean Validation cannot express. */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
