package com.electricity.billing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** US011/US012 - Link a new consumer number (connection) to an existing customer. */
@Data
public class AddConsumerRequest {

    @NotNull(message = "Customer ID is required.")
    private Long customerId;
}
