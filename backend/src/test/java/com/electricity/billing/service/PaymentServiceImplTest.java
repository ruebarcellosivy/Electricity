package com.electricity.billing.service;

import com.electricity.billing.dto.request.PayBillRequest;
import com.electricity.billing.dto.response.PaymentResponse;
import com.electricity.billing.entity.Bill;
import com.electricity.billing.entity.Consumer;
import com.electricity.billing.entity.Customer;
import com.electricity.billing.entity.Payment;
import com.electricity.billing.entity.enums.BillStatus;
import com.electricity.billing.exception.InvalidRequestException;
import com.electricity.billing.exception.ResourceNotFoundException;
import com.electricity.billing.repository.BillRepository;
import com.electricity.billing.repository.CustomerRepository;
import com.electricity.billing.repository.PaymentRepository;
import com.electricity.billing.serviceimpl.PaymentServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private BillRepository billRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Customer customer;
    private Bill unpaidBill;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().id(1L).fullName("John Doe").address("Address").build();
        Consumer consumer = Consumer.builder().id(1L).consumerNumber("1234567890123").customer(customer).build();
        unpaidBill = Bill.builder().id(1L).billNumber("BILL1").consumer(consumer).billingPeriod("JUL-2026")
                .billDate(LocalDate.now()).dueDate(LocalDate.now().plusDays(10))
                .billAmount(new BigDecimal("1000.00")).lateFee(BigDecimal.ZERO).status(BillStatus.UNPAID).build();

        var authentication = new UsernamePasswordAuthenticationToken("johndoe", null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        lenient().when(customerRepository.findByUser_UserId("johndoe")).thenReturn(Optional.of(customer));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private PayBillRequest validRequest(String expiry) {
        PayBillRequest request = new PayBillRequest();
        request.setBillIds(List.of(1L));
        request.setCardNumber("4111111111111111");
        request.setExpiryDate(expiry);
        request.setCvv("123");
        request.setCardHolderName("John Doe");
        request.setPaymentMethod(PayBillRequest.PaymentMethod.CREDIT_CARD);
        return request;
    }

    @Test
    void payBills_marksBillPaidAndReturnsPaymentResponse_whenCardIsValid() {
        String futureExpiry = YearMonth.now().plusYears(2).format(DateTimeFormatter.ofPattern("MM/yy"));
        when(billRepository.findByIdInAndConsumer_Customer_Id(List.of(1L), 1L)).thenReturn(List.of(unpaidBill));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(billRepository.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        List<PaymentResponse> responses = paymentService.payBills(validRequest(futureExpiry));

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getBillNumber()).isEqualTo("BILL1");
        assertThat(unpaidBill.getStatus()).isEqualTo(BillStatus.PAID);
    }

    @Test
    void payBills_throwsInvalidRequestException_whenCardIsExpired() {
        String pastExpiry = YearMonth.now().minusYears(1).format(DateTimeFormatter.ofPattern("MM/yy"));

        assertThatThrownBy(() -> paymentService.payBills(validRequest(pastExpiry)))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("expired");

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void payBills_throwsInvalidRequestException_whenBillAlreadyPaid() {
        unpaidBill.setStatus(BillStatus.PAID);
        String futureExpiry = YearMonth.now().plusYears(2).format(DateTimeFormatter.ofPattern("MM/yy"));
        when(billRepository.findByIdInAndConsumer_Customer_Id(List.of(1L), 1L)).thenReturn(List.of(unpaidBill));

        assertThatThrownBy(() -> paymentService.payBills(validRequest(futureExpiry)))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("already been paid");
    }

    @Test
    void payBills_throwsResourceNotFoundException_whenBillDoesNotBelongToCustomer() {
        String futureExpiry = YearMonth.now().plusYears(2).format(DateTimeFormatter.ofPattern("MM/yy"));
        when(billRepository.findByIdInAndConsumer_Customer_Id(List.of(1L), 1L)).thenReturn(List.of());

        assertThatThrownBy(() -> paymentService.payBills(validRequest(futureExpiry)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
