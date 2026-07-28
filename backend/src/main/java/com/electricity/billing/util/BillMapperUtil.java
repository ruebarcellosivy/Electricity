package com.electricity.billing.util;

import com.electricity.billing.dto.response.BillResponse;
import com.electricity.billing.entity.Bill;

/** Shared Bill -> BillResponse mapping, used by both BillService and CustomerService (home summary). */
public final class BillMapperUtil {

    private BillMapperUtil() {
    }

    public static BillResponse toResponse(Bill bill) {
        return BillResponse.builder()
                .id(bill.getId())
                .billNumber(bill.getBillNumber())
                .consumerNumber(bill.getConsumer().getConsumerNumber())
                .customerName(bill.getConsumer().getCustomer().getFullName())
                .mobileNumber(bill.getConsumer().getCustomer().getMobileNumber())
                .connectionType(bill.getConsumer().getCustomer().getCustomerType())
                .connectionStatus(bill.getConsumer().getConnectionStatus())
                .billingPeriod(bill.getBillingPeriod())
                .billDate(bill.getBillDate())
                .dueDate(bill.getDueDate())
                .disconnectionDate(bill.getDisconnectionDate())
                .billAmount(bill.getBillAmount())
                .lateFee(bill.getLateFee())
                .payableAmount(bill.getPayableAmount())
                .status(bill.getStatus())
                .paymentDate(bill.getPaymentDate())
                .build();
    }
}
