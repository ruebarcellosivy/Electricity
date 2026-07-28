package com.electricity.billing.dto.response;

import com.electricity.billing.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String userId;
    private Role role;
    private String fullName;
    private String customerCode;
    private boolean mustChangePassword;
}
