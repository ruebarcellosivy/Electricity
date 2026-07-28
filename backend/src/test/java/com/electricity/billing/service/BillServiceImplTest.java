package com.electricity.billing.service;

import com.electricity.billing.dto.request.AddBillRequest;
import com.electricity.billing.dto.response.BillResponse;
import com.electricity.billing.entity.Bill;
import com.electricity.billing.entity.Consumer;
import com.electricity.billing.entity.Customer;
import com.electricity.billing.entity.enums.BillStatus;
import com.electricity.billing.entity.enums.ConnectionStatus;
import com.electricity.billing.entity.enums.CustomerType;
import com.electricity.billing.exception.DuplicateRecordException;
import com.electricity.billing.exception.ResourceNotFoundException;
import com.electricity.billing.repository.BillRepository;
import com.electricity.billing.repository.ConsumerRepository;
import com.electricity.billing.repository.CustomerRepository;
import com.electricity.billing.serviceimpl.BillServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceImplTest {

    @Mock
    private BillRepository billRepository;
    @Mock
    private ConsumerRepository consumerRepository;
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private BillServiceImpl billService;

    private Consumer consumer() {
        Customer customer = Customer.builder().id(1L).fullName("John Doe").mobileNumber("9876543210")
                .customerType(CustomerType.RESIDENTIAL).build();
        return Consumer.builder().id(1L).consumerNumber("1234567890123").customer(customer)
                .connectionStatus(ConnectionStatus.CONNECTED).build();
    }

    private AddBillRequest validRequest() {
        AddBillRequest request = new AddBillRequest();
        request.setConsumerNumber("1234567890123");
        request.setBillingPeriod("JUL-2026");
        request.setBillDate(LocalDate.of(2026, 7, 1));
        request.setDueDate(LocalDate.of(2026, 7, 20));
        request.setBillAmount(new BigDecimal("1500.00"));
        request.setLateFee(BigDecimal.ZERO);
        return request;
    }

    @Test
    void addBill_savesBill_whenConsumerExistsAndNoDuplicate() {
        AddBillRequest request = validRequest();
        Consumer consumer = consumer();
        when(consumerRepository.findByConsumerNumber("1234567890123")).thenReturn(Optional.of(consumer));
        when(billRepository.existsByConsumer_ConsumerNumberAndBillingPeriod("1234567890123", "JUL-2026")).thenReturn(false);
        when(billRepository.save(any(Bill.class))).thenAnswer(inv -> {
            Bill b = inv.getArgument(0);
            b.setId(100L);
            return b;
        });

        BillResponse response = billService.addBill(request);

        assertThat(response.getBillNumber()).startsWith("BILL");
        assertThat(response.getBillAmount()).isEqualByComparingTo("1500.00");
        assertThat(response.getStatus()).isEqualTo(BillStatus.UNPAID);
    }

    @Test
    void addBill_throwsResourceNotFoundException_whenConsumerDoesNotExist() {
        AddBillRequest request = validRequest();
        when(consumerRepository.findByConsumerNumber("1234567890123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billService.addBill(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("valid Consumer Number");
    }

    @Test
    void addBill_throwsDuplicateRecordException_whenBillForPeriodAlreadyExists() {
        AddBillRequest request = validRequest();
        Consumer consumer = consumer();
        when(consumerRepository.findByConsumerNumber("1234567890123")).thenReturn(Optional.of(consumer));
        when(billRepository.existsByConsumer_ConsumerNumberAndBillingPeriod("1234567890123", "JUL-2026")).thenReturn(true);

        assertThatThrownBy(() -> billService.addBill(request))
                .isInstanceOf(DuplicateRecordException.class)
                .hasMessageContaining("already exists");

        verify(billRepository, never()).save(any());
    }

    @Test
    void getBill_returnsBill_whenAccessedByAdmin() {
        Consumer consumer = consumer();
        Bill bill = Bill.builder().id(1L).billNumber("BILL1").consumer(consumer).billingPeriod("JUL-2026")
                .billDate(LocalDate.now()).dueDate(LocalDate.now().plusDays(15)).billAmount(BigDecimal.TEN)
                .lateFee(BigDecimal.ZERO).status(BillStatus.UNPAID).build();
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "admin1", null, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));
        var context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);

        try {
            BillResponse response = billService.getBill(1L);
            assertThat(response.getBillNumber()).isEqualTo("BILL1");
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void getBill_throwsResourceNotFoundException_whenBillMissing() {
        when(billRepository.findById(404L)).thenReturn(Optional.empty());

        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "admin1", null, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));
        var context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);

        try {
            assertThatThrownBy(() -> billService.getBill(404L)).isInstanceOf(ResourceNotFoundException.class);
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}
