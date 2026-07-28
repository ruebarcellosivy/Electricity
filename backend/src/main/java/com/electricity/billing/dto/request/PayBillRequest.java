package com.electricity.billing.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.YearMonth;
import java.util.List;

/** US005 - Pay one or more selected bills using a card. */
@Data
public class PayBillRequest {

    public enum PaymentMethod {
        CREDIT_CARD,
        DEBIT_CARD,
        NET_BANKING
    }

    @NotEmpty(message = "Please select at least one bill to pay.")
    private List<Long> billIds;

    @NotBlank(message = "Card number is required.")
    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits.")
    private String cardNumber;

    @NotBlank(message = "Expiry date is required.")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "Expiry date must be in MM/YY format.")
    private String expiryDate;

    @NotBlank(message = "CVV is required.")
    @Pattern(regexp = "\\d{3,4}", message = "CVV must be 3 or 4 digits.")
    private String cvv;

    @NotBlank(message = "Cardholder name is required.")
    @Size(max = 50, message = "Cardholder name cannot exceed 50 characters.")
    private String cardHolderName;

    @NotNull(message = "Payment method is required.")
    private PaymentMethod paymentMethod;

    /** Parses expiryDate (MM/YY) into a comparable YearMonth; returns null if malformed. */
    public YearMonth expiryAsYearMonth() {
        if (expiryDate == null || !expiryDate.matches("^(0[1-9]|1[0-2])/\\d{2}$")) {
            return null;
        }
        String[] parts = expiryDate.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = 2000 + Integer.parseInt(parts[1]);
        return YearMonth.of(year, month);
    }
}
