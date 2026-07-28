package com.electricity.billing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** US011/US012 - Link a new consumer number (connection) to an existing customer. */
@Data
public class AddConsumerRequest {

    @NotNull(message = "Customer is required.")
    private Long customerId;

    @NotBlank(message = "Please enter a valid Consumer Number.")
    @Pattern(regexp = "\\d{13}", message = "Please enter a valid Consumer Number.")
    private String consumerNumber;
}
