package com.electricity.billing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** US002 - Aggregated data for the customer's home/dashboard page. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeSummaryResponse {
    private CustomerResponse profile;
    private BillResponse latestBill;
    private long unpaidBillCount;
    private long openComplaintCount;
}
