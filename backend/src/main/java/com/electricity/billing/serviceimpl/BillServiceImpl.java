package com.electricity.billing.serviceimpl;

import com.electricity.billing.dto.request.AddBillRequest;
import com.electricity.billing.dto.response.BillResponse;
import com.electricity.billing.dto.response.BillSelectionSummaryResponse;
import com.electricity.billing.dto.response.BulkUploadResultResponse;
import com.electricity.billing.dto.response.PageResponse;
import com.electricity.billing.entity.Bill;
import com.electricity.billing.entity.Consumer;
import com.electricity.billing.entity.Customer;
import com.electricity.billing.entity.enums.BillStatus;
import com.electricity.billing.exception.DuplicateRecordException;
import com.electricity.billing.exception.InvalidRequestException;
import com.electricity.billing.exception.ResourceNotFoundException;
import com.electricity.billing.repository.BillRepository;
import com.electricity.billing.repository.ConsumerRepository;
import com.electricity.billing.repository.CustomerRepository;
import com.electricity.billing.service.BillService;
import com.electricity.billing.util.BillMapperUtil;
import com.electricity.billing.util.CsvExportUtil;
import com.electricity.billing.util.IdGeneratorUtil;
import com.electricity.billing.util.PdfGeneratorUtil;
import com.electricity.billing.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final ConsumerRepository consumerRepository;
    private final CustomerRepository customerRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public BillResponse addBill(AddBillRequest request) {
        Consumer consumer = findConsumerOrThrow(request.getConsumerNumber());
        Bill bill = buildAndSaveBill(consumer, request);
        return BillMapperUtil.toResponse(bill);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public BulkUploadResultResponse bulkUpload(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int total = 0;
        int success = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // consumerNumber,billingPeriod,billDate,dueDate,billAmount,lateFee
            if (header == null) {
                throw new InvalidRequestException("The uploaded file is empty.");
            }
            String line;
            int rowNumber = 1;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.isBlank()) {
                    continue;
                }
                total++;
                try {
                    String[] cols = line.split(",", -1);
                    if (cols.length < 5) {
                        throw new InvalidRequestException("Expected at least 5 columns.");
                    }
                    AddBillRequest request = new AddBillRequest();
                    request.setConsumerNumber(cols[0].trim());
                    request.setBillingPeriod(cols[1].trim());
                    request.setBillDate(LocalDate.parse(cols[2].trim()));
                    request.setDueDate(LocalDate.parse(cols[3].trim()));
                    request.setBillAmount(new BigDecimal(cols[4].trim()));
                    request.setLateFee(cols.length > 5 && !cols[5].isBlank() ? new BigDecimal(cols[5].trim()) : BigDecimal.ZERO);

                    if (request.getDueDate().isBefore(request.getBillDate())) {
                        throw new InvalidRequestException("Due Date cannot be before Bill Date.");
                    }
                    Consumer consumer = findConsumerOrThrow(request.getConsumerNumber());
                    buildAndSaveBill(consumer, request);
                    success++;
                } catch (Exception rowEx) {
                    errors.add("Row " + rowNumber + ": " + rowEx.getMessage());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read the uploaded file.", e);
        }

        return BulkUploadResultResponse.builder()
                .totalRows(total)
                .successCount(success)
                .failureCount(total - success)
                .errors(errors)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('CUSTOMER')")
    public PageResponse<BillResponse> myBills(BillStatus status, int page, int size) {
        Customer customer = currentCustomer();
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "billDate"));
        var result = billRepository.findByCustomer(customer.getId(), status, pageable).map(BillMapperUtil::toResponse);
        return PageResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('CUSTOMER')")
    public PageResponse<BillResponse> myBillHistory(LocalDate fromDate, LocalDate toDate, BillStatus status,
                                                     String sortBy, int page, int size) {
        Customer customer = currentCustomer();
        LocalDate from = fromDate != null ? fromDate : LocalDate.now().minusMonths(6);
        LocalDate to = toDate != null ? toDate : LocalDate.now();
        String sortProperty = switch (sortBy == null ? "billDate" : sortBy) {
            case "dueDate" -> "dueDate";
            case "billAmount" -> "billAmount";
            default -> "billDate";
        };
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortProperty));
        var result = billRepository.findByCustomerWithFilters(customer.getId(), status, from, to, pageable)
                .map(BillMapperUtil::toResponse);
        return PageResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('CUSTOMER')")
    public BillSelectionSummaryResponse getSelectionSummary(List<Long> billIds) {
        Customer customer = currentCustomer();
        List<Bill> bills = billRepository.findByIdInAndConsumer_Customer_Id(billIds, customer.getId());
        if (bills.isEmpty()) {
            throw new ResourceNotFoundException("No matching bills were found for your account.");
        }
        BigDecimal total = bills.stream().map(Bill::getPayableAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return BillSelectionSummaryResponse.builder()
                .bills(bills.stream().map(BillMapperUtil::toResponse).toList())
                .totalAmount(total)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<BillResponse> adminSearch(String consumerNumber, String customerCode, BillStatus status,
                                                   int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "billDate"));
        var result = billRepository.searchForAdmin(consumerNumber, customerCode, status, pageable)
                .map(BillMapperUtil::toResponse);
        return PageResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public byte[] exportBillHistory(String consumerNumber, String format) {
        List<Bill> bills;
        String titlePrefix;
        if (consumerNumber == null || consumerNumber.isBlank()) {
            var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                throw new AccessDeniedException("Consumer number is required for customers.");
            }
            bills = billRepository.findAll(Sort.by(Sort.Direction.DESC, "billDate"));
            titlePrefix = "All Bills";
        } else {
            Consumer consumer = findConsumerOrThrow(consumerNumber);
            assertConsumerAccessible(consumer);
            bills = billRepository.findByConsumer_ConsumerNumberOrderByBillDateDesc(consumerNumber);
            titlePrefix = "Consumer " + consumerNumber;
        }

        if ("pdf".equalsIgnoreCase(format)) {
            List<String[]> rows = new ArrayList<>();
            for (Bill b : bills) {
                rows.add(new String[]{b.getBillNumber(), b.getBillingPeriod() + " | Due " + b.getDueDate()
                        + " | Amount " + b.getPayableAmount() + " | " + b.getStatus()});
            }
            return PdfGeneratorUtil.generateDocument("Bill History - " + titlePrefix, rows);
        }
        List<String> headers = List.of("Bill Number", "Consumer Number", "Billing Period", "Bill Date", "Due Date", "Bill Amount",
                "Late Fee", "Status", "Payment Date");
        String csv = CsvExportUtil.toCsv(headers, bills, b -> List.of(
                b.getBillNumber(), b.getConsumer().getConsumerNumber(), b.getBillingPeriod(), b.getBillDate().toString(), b.getDueDate().toString(),
                b.getBillAmount().toString(), b.getLateFee().toString(), b.getStatus().toString(),
                b.getPaymentDate() == null ? "" : b.getPaymentDate().toString()));
        return csv.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getBill(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
        assertBillAccessible(bill);
        return BillMapperUtil.toResponse(bill);
    }

    private Bill buildAndSaveBill(Consumer consumer, AddBillRequest request) {
        if (billRepository.existsByConsumer_ConsumerNumberAndBillingPeriod(consumer.getConsumerNumber(), request.getBillingPeriod())) {
            throw new DuplicateRecordException("A bill for Consumer Number " + consumer.getConsumerNumber()
                    + " and billing period " + request.getBillingPeriod() + " already exists.");
        }
        Bill bill = Bill.builder()
                .billNumber(IdGeneratorUtil.generateBillNumber())
                .consumer(consumer)
                .billingPeriod(request.getBillingPeriod())
                .billDate(request.getBillDate())
                .dueDate(request.getDueDate())
                .disconnectionDate(request.getDisconnectionDate())
                .billAmount(request.getBillAmount())
                .lateFee(request.getLateFee() == null ? BigDecimal.ZERO : request.getLateFee())
                .status(request.getStatus() == null ? BillStatus.UNPAID : request.getStatus())
                .build();
        return billRepository.save(bill);
    }

    private Consumer findConsumerOrThrow(String consumerNumber) {
        return consumerRepository.findByConsumerNumber(consumerNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Please enter a valid Consumer Number."));
    }

    private Customer currentCustomer() {
        String userId = SecurityUtil.currentUserId();
        return customerRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for the current user."));
    }

    private void assertBillAccessible(Bill bill) {
        assertConsumerAccessible(bill.getConsumer());
    }

    private void assertConsumerAccessible(Consumer consumer) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return;
        }
        Customer customer = currentCustomer();
        if (!consumer.getCustomer().getId().equals(customer.getId())) {
            throw new AccessDeniedException("You do not have permission to access this consumer's records.");
        }
    }
}
