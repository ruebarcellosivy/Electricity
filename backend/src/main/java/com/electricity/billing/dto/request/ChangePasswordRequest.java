package com.electricity.billing.dto.request;

import com.electricity.billing.validation.PasswordConfirmable;
import com.electricity.billing.validation.PasswordMatches;
import com.electricity.billing.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@PasswordMatches
public class ChangePasswordRequest implements PasswordConfirmable {

    @NotBlank(message = "Current password is required.")
    private String oldPassword;

    @NotBlank(message = "New password is required.")
    @StrongPassword
    private String password;

    @NotBlank(message = "Please confirm your new password.")
    private String confirmPassword;
}
