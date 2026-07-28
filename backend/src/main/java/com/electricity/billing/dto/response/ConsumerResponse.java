package com.electricity.billing.dto.response;

import com.electricity.billing.entity.enums.ConnectionStatus;
import com.electricity.billing.entity.enums.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumerResponse {
    private Long id;
    private String consumerNumber;
    private ConnectionStatus connectionStatus;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private CustomerType customerType;
    private LocalDateTime createdAt;
}
