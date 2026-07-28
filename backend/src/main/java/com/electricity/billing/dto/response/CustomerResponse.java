package com.electricity.billing.dto.response;

import com.electricity.billing.entity.enums.CustomerStatus;
import com.electricity.billing.entity.enums.CustomerType;
import com.electricity.billing.entity.enums.ElectricalSection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
    private Long id;
    private String customerCode;
    private String fullName;
    private String address;
    private String email;
    private String mobileNumber;
    private CustomerType customerType;
    private ElectricalSection electricalSection;
    private CustomerStatus status;
    private String userId;
    private List<String> consumerNumbers;
    private LocalDateTime createdAt;
}
