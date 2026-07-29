package com.electricity.billing.dto.request;

import com.electricity.billing.entity.enums.CustomerType;
import com.electricity.billing.entity.enums.ElectricalSection;
import com.electricity.billing.validation.PasswordConfirmable;
import com.electricity.billing.validation.PasswordMatches;
import com.electricity.billing.validation.StrongPassword;
import jakarta.validation.constraints.*;
import lombok.Data;

/** US001 - Customer self-registration request. */
@Data
@PasswordMatches
public class RegisterRequest implements PasswordConfirmable {


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

    @NotNull(message = "Electrical section is required.")
    private ElectricalSection electricalSection;


    @NotBlank(message = "Password is required.")
    @StrongPassword
    private String password;

    @NotBlank(message = "Please confirm your password.")
    private String confirmPassword;
}
