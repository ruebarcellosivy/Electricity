package com.electricity.billing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {
    private String userId;
    private String customerCode;
    private String consumerNumber;
    private String fullName;
    private String email;
    private String message;
}
