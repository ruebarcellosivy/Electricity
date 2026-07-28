package com.electricity.billing.controller;

import com.electricity.billing.dto.request.PayBillRequest;
import com.electricity.billing.dto.response.InvoiceResponse;
import com.electricity.billing.dto.response.PaymentResponse;
import com.electricity.billing.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** US005 (pay bills) and US006 (generate invoice) endpoints. */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/pay")
    public ResponseEntity<List<PaymentResponse>> pay(@Valid @RequestBody PayBillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.payBills(request));
    }

    @GetMapping("/invoice/{transactionId}")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable String transactionId) {
        return ResponseEntity.ok(paymentService.getInvoice(transactionId));
    }

    @GetMapping("/receipt/{paymentId}/download")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable String paymentId) {
        byte[] content = paymentService.getReceiptPdf(paymentId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"receipt-" + paymentId + ".pdf\"")
                .body(content);
    }

    @GetMapping("/invoice/{transactionId}/download")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable String transactionId) {
        byte[] content = paymentService.getInvoicePdf(transactionId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"invoice-" + transactionId + ".pdf\"")
                .body(content);
    }
}
