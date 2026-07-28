package com.electricity.billing.dto.response;

import com.electricity.billing.entity.enums.TransactionStatus;
import com.electricity.billing.entity.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private String paymentId;
    private String transactionId;
    private String receiptNumber;
    private LocalDateTime transactionDate;
    private TransactionType transactionType;
    private String billNumber;
    private BigDecimal transactionAmount;
    private TransactionStatus transactionStatus;
}
