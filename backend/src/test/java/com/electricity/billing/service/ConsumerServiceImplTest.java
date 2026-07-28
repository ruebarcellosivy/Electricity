package com.electricity.billing.service;

import com.electricity.billing.dto.request.AddConsumerRequest;
import com.electricity.billing.dto.request.ConnectionStatusUpdateRequest;
import com.electricity.billing.dto.response.ConsumerResponse;
import com.electricity.billing.entity.Consumer;
import com.electricity.billing.entity.Customer;
import com.electricity.billing.entity.enums.ConnectionStatus;
import com.electricity.billing.entity.enums.CustomerType;
import com.electricity.billing.exception.DuplicateRecordException;
import com.electricity.billing.exception.ResourceNotFoundException;
import com.electricity.billing.repository.ConsumerRepository;
import com.electricity.billing.repository.CustomerRepository;
import com.electricity.billing.serviceimpl.ConsumerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumerServiceImplTest {

    @Mock
    private ConsumerRepository consumerRepository;
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private ConsumerServiceImpl consumerService;

    @Test
    void addConsumer_savesConsumer_whenNumberIsUnique() {
        Customer customer = Customer.builder().id(1L).customerCode("CUST1").fullName("John Doe")
                .customerType(CustomerType.RESIDENTIAL).build();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(consumerRepository.existsByConsumerNumber("9998887776665")).thenReturn(false);
        when(consumerRepository.save(any(Consumer.class))).thenAnswer(inv -> {
            Consumer c = inv.getArgument(0);
            c.setId(2L);
            return c;
        });

        AddConsumerRequest request = new AddConsumerRequest();
        request.setCustomerId(1L);
        request.setConsumerNumber("9998887776665");

        ConsumerResponse response = consumerService.addConsumer(request);

        assertThat(response.getConsumerNumber()).isEqualTo("9998887776665");
        assertThat(response.getConnectionStatus()).isEqualTo(ConnectionStatus.CONNECTED);
    }

    @Test
    void addConsumer_throwsDuplicateRecordException_whenConsumerNumberAlreadyExists() {
        Customer customer = Customer.builder().id(1L).customerCode("CUST1").build();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(consumerRepository.existsByConsumerNumber("9998887776665")).thenReturn(true);

        AddConsumerRequest request = new AddConsumerRequest();
        request.setCustomerId(1L);
        request.setConsumerNumber("9998887776665");

        assertThatThrownBy(() -> consumerService.addConsumer(request))
                .isInstanceOf(DuplicateRecordException.class);
    }

    @Test
    void updateConnectionStatus_disconnectsConsumer() {
        Customer customer = Customer.builder().id(1L).customerCode("CUST1").build();
        Consumer consumer = Consumer.builder().id(1L).consumerNumber("1234567890123").customer(customer)
                .connectionStatus(ConnectionStatus.CONNECTED).build();
        when(consumerRepository.findByConsumerNumber("1234567890123")).thenReturn(Optional.of(consumer));
        when(consumerRepository.save(any(Consumer.class))).thenAnswer(inv -> inv.getArgument(0));

        ConnectionStatusUpdateRequest request = new ConnectionStatusUpdateRequest();
        request.setAction(ConnectionStatusUpdateRequest.Action.DISCONNECT);

        ConsumerResponse response = consumerService.updateConnectionStatus("1234567890123", request);

        assertThat(response.getConnectionStatus()).isEqualTo(ConnectionStatus.DISCONNECTED);
    }

    @Test
    void updateConnectionStatus_throwsResourceNotFoundException_whenConsumerMissing() {
        when(consumerRepository.findByConsumerNumber("0000000000000")).thenReturn(Optional.empty());

        ConnectionStatusUpdateRequest request = new ConnectionStatusUpdateRequest();
        request.setAction(ConnectionStatusUpdateRequest.Action.RECONNECT);

        assertThatThrownBy(() -> consumerService.updateConnectionStatus("0000000000000", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
