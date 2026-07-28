package com.electricity.billing.service;

import com.electricity.billing.dto.request.AddConsumerRequest;
import com.electricity.billing.dto.request.ConnectionStatusUpdateRequest;
import com.electricity.billing.dto.response.ConsumerResponse;
import com.electricity.billing.dto.response.PageResponse;
import com.electricity.billing.entity.enums.CustomerType;
import com.electricity.billing.entity.enums.ElectricalSection;

import java.util.List;

public interface ConsumerService {

    ConsumerResponse addConsumer(AddConsumerRequest request);

    PageResponse<ConsumerResponse> listConsumers(ElectricalSection section, CustomerType type, int page, int size);

    ConsumerResponse updateConnectionStatus(String consumerNumber, ConnectionStatusUpdateRequest request);

    List<ConsumerResponse> myConsumers();
}
