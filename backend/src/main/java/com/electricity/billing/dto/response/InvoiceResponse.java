package com.electricity.billing.dto.response;

import com.electricity.billing.entity.enums.TransactionStatus;
import com.electricity.billing.entity.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** US006 - Invoice generated for a completed payment transaction. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {
    private String invoiceNumber;
    private String paymentId;
    private String transactionId;
    private String receiptNumber;
    private String consumerNumber;
    private String customerName;
    private String address;
    private LocalDateTime transactionDate;
    private TransactionType transactionType;
    private String billNumber;
    private BigDecimal transactionAmount;
    private TransactionStatus transactionStatus;
}
