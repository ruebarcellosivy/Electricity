package com.electricity.billing.validation;

/** Implemented by any request DTO that carries a password + confirmPassword pair. */
public interface PasswordConfirmable {
    String getPassword();
    String getConfirmPassword();
}
