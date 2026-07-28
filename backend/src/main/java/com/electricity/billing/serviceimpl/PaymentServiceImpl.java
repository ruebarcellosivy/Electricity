package com.electricity.billing.serviceimpl;

import com.electricity.billing.dto.request.PayBillRequest;
import com.electricity.billing.dto.response.InvoiceResponse;
import com.electricity.billing.dto.response.PaymentResponse;
import com.electricity.billing.entity.Bill;
import com.electricity.billing.entity.Customer;
import com.electricity.billing.entity.Payment;
import com.electricity.billing.entity.enums.BillStatus;
import com.electricity.billing.entity.enums.TransactionStatus;
import com.electricity.billing.entity.enums.TransactionType;
import com.electricity.billing.exception.InvalidRequestException;
import com.electricity.billing.exception.ResourceNotFoundException;
import com.electricity.billing.repository.BillRepository;
import com.electricity.billing.repository.CustomerRepository;
import com.electricity.billing.repository.PaymentRepository;
import com.electricity.billing.service.PaymentService;
import com.electricity.billing.util.IdGeneratorUtil;
import com.electricity.billing.util.PdfGeneratorUtil;
import com.electricity.billing.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;

    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<PaymentResponse> payBills(PayBillRequest request) {
        YearMonth expiry = request.expiryAsYearMonth();
        if (expiry == null || expiry.isBefore(YearMonth.now())) {
            throw new InvalidRequestException("The card has expired. Please use a valid card.");
        }

        Customer customer = currentCustomer();
        List<Bill> bills = billRepository.findByIdInAndConsumer_Customer_Id(request.getBillIds(), customer.getId());
        if (bills.size() != request.getBillIds().size()) {
            throw new ResourceNotFoundException("One or more selected bills could not be found for your account.");
        }
        boolean alreadyPaid = bills.stream().anyMatch(b -> b.getStatus() == BillStatus.PAID);
        if (alreadyPaid) {
            throw new InvalidRequestException("One or more selected bills have already been paid.");
        }

        TransactionType transactionType = request.getPaymentMethod() == PayBillRequest.PaymentMethod.CREDIT_CARD
                ? TransactionType.CREDIT : TransactionType.DEBIT;
        String cardLast4 = request.getCardNumber().substring(request.getCardNumber().length() - 4);

        List<PaymentResponse> responses = new ArrayList<>();
        for (Bill bill : bills) {
            Payment payment = Payment.builder()
                    .paymentId(IdGeneratorUtil.generatePaymentId())
                    .transactionId(IdGeneratorUtil.generateTransactionId())
                    .receiptNumber(IdGeneratorUtil.generateReceiptNumber())
                    .bill(bill)
                    .transactionDate(LocalDateTime.now())
                    .transactionType(transactionType)
                    .transactionAmount(bill.getPayableAmount())
                    .transactionStatus(TransactionStatus.SUCCESS)
                    .cardHolderName(request.getCardHolderName())
                    .cardLast4(cardLast4)
                    .build();
            paymentRepository.save(payment);

            bill.setStatus(BillStatus.PAID);
            bill.setPaymentDate(LocalDate.now());
            billRepository.save(bill);

            responses.add(toPaymentResponse(payment));
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(String transactionId) {
        Payment payment = findPaymentByTransactionId(transactionId);
        assertPaymentAccessible(payment);
        return toInvoiceResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getReceiptPdf(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        assertPaymentAccessible(payment);

        List<String[]> rows = List.of(
                new String[]{"Payment ID", payment.getPaymentId()},
                new String[]{"Transaction ID", payment.getTransactionId()},
                new String[]{"Receipt Number", payment.getReceiptNumber()},
                new String[]{"Bill Number", payment.getBill().getBillNumber()},
                new String[]{"Consumer Number", payment.getBill().getConsumer().getConsumerNumber()},
                new String[]{"Transaction Date", payment.getTransactionDate().toString()},
                new String[]{"Transaction Type", payment.getTransactionType().toString()},
                new String[]{"Transaction Amount", payment.getTransactionAmount().toString()},
                new String[]{"Transaction Status", payment.getTransactionStatus().toString()}
        );
        return PdfGeneratorUtil.generateDocument("Payment Receipt", rows);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getInvoicePdf(String transactionId) {
        Payment payment = findPaymentByTransactionId(transactionId);
        assertPaymentAccessible(payment);
        InvoiceResponse invoice = toInvoiceResponse(payment);

        List<String[]> rows = List.of(
                new String[]{"Invoice Number", invoice.getInvoiceNumber()},
                new String[]{"Payment ID", invoice.getPaymentId()},
                new String[]{"Transaction ID", invoice.getTransactionId()},
                new String[]{"Receipt Number", invoice.getReceiptNumber()},
                new String[]{"Consumer Number", invoice.getConsumerNumber()},
                new String[]{"Customer Name", invoice.getCustomerName()},
                new String[]{"Address", invoice.getAddress()},
                new String[]{"Bill Number", invoice.getBillNumber()},
                new String[]{"Transaction Date", invoice.getTransactionDate().toString()},
                new String[]{"Transaction Type", invoice.getTransactionType().toString()},
                new String[]{"Transaction Amount", invoice.getTransactionAmount().toString()},
                new String[]{"Transaction Status", invoice.getTransactionStatus().toString()}
        );
        return PdfGeneratorUtil.generateDocument("Invoice", rows);
    }

    private Payment findPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for transaction id: " + transactionId));
    }

    private InvoiceResponse toInvoiceResponse(Payment payment) {
        Customer customer = payment.getBill().getConsumer().getCustomer();
        return InvoiceResponse.builder()
                .invoiceNumber("INV" + payment.getTransactionId().replaceFirst("^TXN", ""))
                .paymentId(payment.getPaymentId())
                .transactionId(payment.getTransactionId())
                .receiptNumber(payment.getReceiptNumber())
                .consumerNumber(payment.getBill().getConsumer().getConsumerNumber())
                .customerName(customer.getFullName())
                .address(customer.getAddress())
                .transactionDate(payment.getTransactionDate())
                .transactionType(payment.getTransactionType())
                .billNumber(payment.getBill().getBillNumber())
                .transactionAmount(payment.getTransactionAmount())
                .transactionStatus(payment.getTransactionStatus())
                .build();
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .transactionId(payment.getTransactionId())
                .receiptNumber(payment.getReceiptNumber())
                .transactionDate(payment.getTransactionDate())
                .transactionType(payment.getTransactionType())
                .billNumber(payment.getBill().getBillNumber())
                .transactionAmount(payment.getTransactionAmount())
                .transactionStatus(payment.getTransactionStatus())
                .build();
    }

    private Customer currentCustomer() {
        String userId = SecurityUtil.currentUserId();
        return customerRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for the current user."));
    }

    private void assertPaymentAccessible(Payment payment) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return;
        }
        Customer customer = currentCustomer();
        if (!payment.getBill().getConsumer().getCustomer().getId().equals(customer.getId())) {
            throw new AccessDeniedException("You do not have permission to view this payment.");
        }
    }
}
