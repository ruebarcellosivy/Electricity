package com.electricity.billing.service;

import com.electricity.billing.dto.request.PayBillRequest;
import com.electricity.billing.dto.response.InvoiceResponse;
import com.electricity.billing.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {

    List<PaymentResponse> payBills(PayBillRequest request);

    InvoiceResponse getInvoice(String transactionId);

    byte[] getReceiptPdf(String paymentId);

    byte[] getInvoicePdf(String transactionId);
}
