package com.electricity.billing.dto.response;

import com.electricity.billing.entity.enums.BillStatus;
import com.electricity.billing.entity.enums.ConnectionStatus;
import com.electricity.billing.entity.enums.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillResponse {
    private Long id;
    private String billNumber;
    private String consumerNumber;
    private String customerName;
    private String mobileNumber;
    private CustomerType connectionType;
    private ConnectionStatus connectionStatus;
    private String billingPeriod;
    private LocalDate billDate;
    private LocalDate dueDate;
    private LocalDate disconnectionDate;
    private BigDecimal billAmount;
    private BigDecimal lateFee;
    private BigDecimal payableAmount;
    private BillStatus status;
    private LocalDate paymentDate;
}
