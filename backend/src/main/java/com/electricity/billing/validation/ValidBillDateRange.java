package com.electricity.billing.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/** Class-level constraint ensuring a bill's due date is not before its bill date. */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidBillDateRangeValidator.class)
@Documented
public @interface ValidBillDateRange {
    String message() default "Due Date cannot be before Bill Date.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
