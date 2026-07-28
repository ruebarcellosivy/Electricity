package com.electricity.billing.dto.request;

import com.electricity.billing.entity.enums.BillStatus;
import com.electricity.billing.validation.DateRangeValidatable;
import com.electricity.billing.validation.ValidBillDateRange;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** US015 - Admin adds a bill for a consumer. */
@Data
@ValidBillDateRange
public class AddBillRequest implements DateRangeValidatable {

    @NotBlank(message = "Please enter a valid Consumer Number.")
    private String consumerNumber;

    @NotBlank(message = "Billing period is required.")
    private String billingPeriod;

    @NotNull(message = "Bill date is required.")
    private LocalDate billDate;

    @NotNull(message = "Due date is required.")
    private LocalDate dueDate;

    private LocalDate disconnectionDate;

    @NotNull(message = "Bill amount is required.")
    @DecimalMin(value = "0.0", message = "Bill amount cannot be negative.")
    private BigDecimal billAmount;

    @DecimalMin(value = "0.0", message = "Late fee cannot be negative.")
    private BigDecimal lateFee = BigDecimal.ZERO;

    private BillStatus status = BillStatus.UNPAID;
}
