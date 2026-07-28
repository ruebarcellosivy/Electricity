package com.electricity.billing.controller;

import com.electricity.billing.dto.request.ComplaintRequest;
import com.electricity.billing.dto.request.ComplaintStatusUpdateRequest;
import com.electricity.billing.dto.response.ComplaintResponse;
import com.electricity.billing.dto.response.PageResponse;
import com.electricity.billing.entity.enums.ComplaintStatus;
import com.electricity.billing.entity.enums.ComplaintType;
import com.electricity.billing.service.ComplaintService;
import com.electricity.billing.util.ComplaintCategoryUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** US008-US010 (Customer) and US017-US019 (Admin/SME) complaint endpoints. */
@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @GetMapping("/categories")
    public ResponseEntity<Map<ComplaintType, List<String>>> categories() {
        return ResponseEntity.ok(ComplaintCategoryUtil.allCategories());
    }

    @PostMapping
    public ResponseEntity<ComplaintResponse> register(@Valid @RequestBody ComplaintRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(complaintService.register(request));
    }

    @GetMapping("/track/{complaintNumber}")
    public ResponseEntity<ComplaintResponse> track(@PathVariable String complaintNumber) {
        return ResponseEntity.ok(complaintService.getByComplaintNumber(complaintNumber));
    }

    @GetMapping("/me")
    public ResponseEntity<PageResponse<ComplaintResponse>> myHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(complaintService.myHistory(page, size));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ComplaintResponse>> search(
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String consumerNumber,
            @RequestParam(required = false) String complaintNumber,
            @RequestParam(required = false) ComplaintType complaintType,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(complaintService.search(customerCode, consumerNumber, complaintNumber, complaintType,
                status, fromDate, toDate, page, size));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ComplaintResponse> updateStatus(@PathVariable Long id,
                                                           @Valid @RequestBody ComplaintStatusUpdateRequest request) {
        return ResponseEntity.ok(complaintService.updateStatus(id, request));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String consumerNumber,
            @RequestParam(required = false) ComplaintType complaintType,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(defaultValue = "csv") String format) {
        byte[] content = complaintService.exportComplaints(customerCode, consumerNumber, complaintType, status, format);
        String filename = "complaints" + (format.equalsIgnoreCase("pdf") ? ".pdf" : ".csv");
        MediaType mediaType = format.equalsIgnoreCase("pdf") ? MediaType.APPLICATION_PDF : MediaType.parseMediaType("text/csv");
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(content);
    }
}
