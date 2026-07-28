package com.electricity.billing.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidBillDateRangeValidator implements ConstraintValidator<ValidBillDateRange, DateRangeValidatable> {

    @Override
    public boolean isValid(DateRangeValidatable value, ConstraintValidatorContext context) {
        if (value == null || value.getBillDate() == null || value.getDueDate() == null) {
            return true;
        }
        boolean valid = !value.getDueDate().isBefore(value.getBillDate());
        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("dueDate")
                    .addConstraintViolation();
        }
        return valid;
    }
}
