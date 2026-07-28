package com.electricity.billing.validation;

import java.time.LocalDate;

/** Implemented by any request DTO that carries a billDate + dueDate pair. */
public interface DateRangeValidatable {
    LocalDate getBillDate();
    LocalDate getDueDate();
}
