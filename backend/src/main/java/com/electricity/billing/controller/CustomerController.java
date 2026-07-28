package com.electricity.billing.controller;

import com.electricity.billing.dto.request.AdminCreateCustomerRequest;
import com.electricity.billing.dto.request.UpdateCustomerRequest;
import com.electricity.billing.dto.response.CustomerResponse;
import com.electricity.billing.dto.response.HomeSummaryResponse;
import com.electricity.billing.dto.response.MessageResponse;
import com.electricity.billing.dto.response.PageResponse;
import com.electricity.billing.entity.enums.CustomerType;
import com.electricity.billing.entity.enums.ElectricalSection;
import com.electricity.billing.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** US011/US012/US013 (Admin) and US002 (Customer home/profile) customer management endpoints. */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody AdminCreateCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CustomerResponse>> listCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ElectricalSection electricalSection,
            @RequestParam(required = false) CustomerType customerType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(customerService.listCustomers(search, electricalSection, customerType, page, size));
    }

    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> myProfile() {
        return ResponseEntity.ok(customerService.getMyProfile());
    }

    @GetMapping("/me/home")
    public ResponseEntity<HomeSummaryResponse> myHomeSummary() {
        return ResponseEntity.ok(customerService.getHomeSummary());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomer(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(MessageResponse.of("Customer deactivated successfully."));
    }
}
