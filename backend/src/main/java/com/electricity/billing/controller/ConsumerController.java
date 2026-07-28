package com.electricity.billing.controller;

import com.electricity.billing.dto.request.AddConsumerRequest;
import com.electricity.billing.dto.request.ConnectionStatusUpdateRequest;
import com.electricity.billing.dto.response.ConsumerResponse;
import com.electricity.billing.dto.response.PageResponse;
import com.electricity.billing.entity.enums.CustomerType;
import com.electricity.billing.entity.enums.ElectricalSection;
import com.electricity.billing.service.ConsumerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** US011 (link consumer), US012 (list) and US014 (disconnect/reconnect) endpoints. */
@RestController
@RequestMapping("/api/consumers")
@RequiredArgsConstructor
public class ConsumerController {

    private final ConsumerService consumerService;

    @PostMapping
    public ResponseEntity<ConsumerResponse> addConsumer(@Valid @RequestBody AddConsumerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consumerService.addConsumer(request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ConsumerResponse>> listConsumers(
            @RequestParam(required = false) ElectricalSection electricalSection,
            @RequestParam(required = false) CustomerType customerType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(consumerService.listConsumers(electricalSection, customerType, page, size));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ConsumerResponse>> myConsumers() {
        return ResponseEntity.ok(consumerService.myConsumers());
    }

    @PutMapping("/{consumerNumber}/connection-status")
    public ResponseEntity<ConsumerResponse> updateConnectionStatus(@PathVariable String consumerNumber,
                                                                    @Valid @RequestBody ConnectionStatusUpdateRequest request) {
        return ResponseEntity.ok(consumerService.updateConnectionStatus(consumerNumber, request));
    }
}
