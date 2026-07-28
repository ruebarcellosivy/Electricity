package com.electricity.billing.serviceimpl;

import com.electricity.billing.dto.request.AddConsumerRequest;
import com.electricity.billing.dto.request.ConnectionStatusUpdateRequest;
import com.electricity.billing.dto.response.ConsumerResponse;
import com.electricity.billing.dto.response.PageResponse;
import com.electricity.billing.entity.Consumer;
import com.electricity.billing.entity.Customer;
import com.electricity.billing.entity.enums.ConnectionStatus;
import com.electricity.billing.entity.enums.CustomerType;
import com.electricity.billing.entity.enums.ElectricalSection;
import com.electricity.billing.exception.DuplicateRecordException;
import com.electricity.billing.exception.ResourceNotFoundException;
import com.electricity.billing.repository.ConsumerRepository;
import com.electricity.billing.repository.CustomerRepository;
import com.electricity.billing.service.ConsumerService;
import com.electricity.billing.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConsumerServiceImpl implements ConsumerService {

    private final ConsumerRepository consumerRepository;
    private final CustomerRepository customerRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ConsumerResponse addConsumer(AddConsumerRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        long count = consumerRepository.count();
        String generatedConsumerNumber = "CON" + (count + 1);
        while (consumerRepository.existsByConsumerNumber(generatedConsumerNumber)) {
            count++;
            generatedConsumerNumber = "CON" + (count + 1);
        }

        Consumer consumer = consumerRepository.save(Consumer.builder()
                .consumerNumber(generatedConsumerNumber)
                .customer(customer)
                .connectionStatus(ConnectionStatus.CONNECTED)
                .build());

        return toResponse(consumer);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<ConsumerResponse> listConsumers(ElectricalSection section, CustomerType type, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = consumerRepository.search(section, type, pageable).map(this::toResponse);
        return PageResponse.from(result);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ConsumerResponse updateConnectionStatus(String consumerNumber, ConnectionStatusUpdateRequest request) {
        Consumer consumer = consumerRepository.findByConsumerNumber(consumerNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Consumer not found with number: " + consumerNumber));

        consumer.setConnectionStatus(request.getAction() == ConnectionStatusUpdateRequest.Action.DISCONNECT
                ? ConnectionStatus.DISCONNECTED
                : ConnectionStatus.CONNECTED);

        return toResponse(consumerRepository.save(consumer));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumerResponse> myConsumers() {
        String userId = SecurityUtil.currentUserId();
        Customer customer = customerRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for the current user."));
        return consumerRepository.findByCustomer_Id(customer.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ConsumerResponse toResponse(Consumer consumer) {
        Customer customer = consumer.getCustomer();
        return ConsumerResponse.builder()
                .id(consumer.getId())
                .consumerNumber(consumer.getConsumerNumber())
                .connectionStatus(consumer.getConnectionStatus())
                .customerId(customer.getId())
                .customerCode(customer.getCustomerCode())
                .customerName(customer.getFullName())
                .customerType(customer.getCustomerType())
                .createdAt(consumer.getCreatedAt())
                .build();
    }
}
