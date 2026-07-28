package com.electricity.billing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "User ID is required.")
    private String userId;

    @NotBlank(message = "Password is required.")
    private String password;
}
