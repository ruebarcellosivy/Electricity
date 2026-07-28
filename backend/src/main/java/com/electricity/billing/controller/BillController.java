package com.electricity.billing.controller;

import com.electricity.billing.dto.request.AddBillRequest;
import com.electricity.billing.dto.response.BillResponse;
import com.electricity.billing.dto.response.BillSelectionSummaryResponse;
import com.electricity.billing.dto.response.BulkUploadResultResponse;
import com.electricity.billing.dto.response.PageResponse;
import com.electricity.billing.entity.enums.BillStatus;
import com.electricity.billing.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/** US003/US004/US007 (Customer) and US015/US016 (Admin) bill endpoints. */
@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping
    public ResponseEntity<BillResponse> addBill(@Valid @RequestBody AddBillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billService.addBill(request));
    }

    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkUploadResultResponse> bulkUpload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(billService.bulkUpload(file));
    }

    @GetMapping("/me")
    public ResponseEntity<PageResponse<BillResponse>> myBills(
            @RequestParam(required = false) BillStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(billService.myBills(status, page, size));
    }

    @GetMapping("/me/history")
    public ResponseEntity<PageResponse<BillResponse>> myBillHistory(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) BillStatus status,
            @RequestParam(defaultValue = "billDate") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(billService.myBillHistory(fromDate, toDate, status, sortBy, page, size));
    }

    @PostMapping("/me/selection-summary")
    public ResponseEntity<BillSelectionSummaryResponse> selectionSummary(@RequestBody List<Long> billIds) {
        return ResponseEntity.ok(billService.getSelectionSummary(billIds));
    }

    @GetMapping
    public ResponseEntity<PageResponse<BillResponse>> adminSearch(
            @RequestParam(required = false) String consumerNumber,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) BillStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(billService.adminSearch(consumerNumber, customerCode, status, page, size));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportBillHistory(@RequestParam String consumerNumber,
                                                     @RequestParam(defaultValue = "csv") String format) {
        byte[] content = billService.exportBillHistory(consumerNumber, format);
        String filename = "bill-history-" + consumerNumber + (format.equalsIgnoreCase("pdf") ? ".pdf" : ".csv");
        MediaType mediaType = format.equalsIgnoreCase("pdf") ? MediaType.APPLICATION_PDF : MediaType.parseMediaType("text/csv");
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(content);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillResponse> getBill(@PathVariable Long id) {
        return ResponseEntity.ok(billService.getBill(id));
    }
}
