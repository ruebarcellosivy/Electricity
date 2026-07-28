package com.electricity.billing.dto.request;

import com.electricity.billing.entity.enums.CustomerType;
import jakarta.validation.constraints.*;
import lombok.Data;

/** US013 - Admin updates an existing customer. Customer ID and Consumer Number are not editable. */
@Data
public class UpdateCustomerRequest {

    @NotBlank(message = "Full name cannot be empty.")
    @Size(max = 50, message = "Full name cannot exceed 50 characters.")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Full name should contain only letters.")
    private String fullName;

    @NotBlank(message = "Address is required.")
    @Size(min = 10, max = 200, message = "Address must be between 10 and 200 characters.")
    private String address;

    @NotBlank(message = "Email is required.")
    @Email(message = "Please enter a valid email address.")
    private String email;

    @NotBlank(message = "Mobile number is invalid.")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number is invalid.")
    private String mobileNumber;

    @NotNull(message = "Customer type is required.")
    private CustomerType customerType;
}
