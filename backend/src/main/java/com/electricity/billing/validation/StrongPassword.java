package com.electricity.billing.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Enforces the password complexity rules from the SRS: minimum 8 characters,
 * at least one uppercase letter, one lowercase letter and one special character.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
@Documented
public @interface StrongPassword {
    String message() default "Password must be at least 8 characters long and include an uppercase letter, " +
            "a lowercase letter and a special character.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
