package com.electricity.billing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/** US004 - Summary of the bills a customer has selected for payment. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillSelectionSummaryResponse {
    private List<BillResponse> bills;
    private BigDecimal totalAmount;
}
