package com.electricity.billing.exception;

/** Thrown for login failures - invalid user id or password. */
public class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException(String message) {
        super(message);
    }
}
