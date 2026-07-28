package com.electricity.billing.service;

import com.electricity.billing.dto.request.ComplaintRequest;
import com.electricity.billing.dto.request.ComplaintStatusUpdateRequest;
import com.electricity.billing.dto.response.ComplaintResponse;
import com.electricity.billing.entity.Complaint;
import com.electricity.billing.entity.Consumer;
import com.electricity.billing.entity.Customer;
import com.electricity.billing.entity.enums.ComplaintStatus;
import com.electricity.billing.entity.enums.ComplaintType;
import com.electricity.billing.entity.enums.ContactMethod;
import com.electricity.billing.exception.InvalidRequestException;
import com.electricity.billing.exception.ResourceNotFoundException;
import com.electricity.billing.repository.ComplaintRepository;
import com.electricity.billing.repository.ConsumerRepository;
import com.electricity.billing.repository.CustomerRepository;
import com.electricity.billing.serviceimpl.ComplaintServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceImplTest {

    @Mock
    private ComplaintRepository complaintRepository;
    @Mock
    private ConsumerRepository consumerRepository;
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private ComplaintServiceImpl complaintService;

    private Customer customer;
    private Consumer consumer;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().id(1L).fullName("John Doe").build();
        consumer = Consumer.builder().id(1L).consumerNumber("1234567890123").customer(customer).build();

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

    private ComplaintRequest validRequest() {
        ComplaintRequest request = new ComplaintRequest();
        request.setConsumerNumber("1234567890123");
        request.setComplaintType(ComplaintType.POWER_OUTAGE);
        request.setCategory("Full Outage");
        request.setDescription("No power since morning.");
        request.setPreferredContactMethod(ContactMethod.PHONE);
        request.setContactDetails("9876543210");
        return request;
    }

    @Test
    void register_savesComplaint_whenConsumerBelongsToCustomerAndCategoryValid() {
        when(consumerRepository.findByConsumerNumber("1234567890123")).thenReturn(Optional.of(consumer));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> {
            Complaint c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        ComplaintResponse response = complaintService.register(validRequest());

        assertThat(response.getComplaintNumber()).startsWith("CMP");
        assertThat(response.getStatus()).isEqualTo(ComplaintStatus.OPEN);
    }

    @Test
    void register_throwsInvalidRequestException_whenCategoryDoesNotMatchType() {
        when(consumerRepository.findByConsumerNumber("1234567890123")).thenReturn(Optional.of(consumer));
        ComplaintRequest request = validRequest();
        request.setCategory("Faulty Meter"); // belongs to METER_ISSUE, not POWER_OUTAGE

        assertThatThrownBy(() -> complaintService.register(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Invalid category");
    }

    @Test
    void register_throwsAccessDeniedException_whenConsumerNotOwnedByCustomer() {
        Customer otherCustomer = Customer.builder().id(2L).fullName("Other Person").build();
        Consumer otherConsumer = Consumer.builder().id(2L).consumerNumber("1234567890123").customer(otherCustomer).build();
        when(consumerRepository.findByConsumerNumber("1234567890123")).thenReturn(Optional.of(otherConsumer));

        assertThatThrownBy(() -> complaintService.register(validRequest()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void register_throwsResourceNotFoundException_whenConsumerNumberUnknown() {
        when(consumerRepository.findByConsumerNumber("1234567890123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> complaintService.register(validRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_addsRemarkAndChangesStatus() {
        Complaint complaint = Complaint.builder().id(5L).complaintNumber("CMP1").consumer(consumer)
                .complaintType(ComplaintType.POWER_OUTAGE).category("Full Outage").description("desc")
                .preferredContactMethod(ContactMethod.PHONE).contactDetails("123")
                .status(ComplaintStatus.OPEN).remarks(new java.util.ArrayList<>()).build();
        when(complaintRepository.findById(5L)).thenReturn(Optional.of(complaint));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> inv.getArgument(0));

        ComplaintStatusUpdateRequest request = new ComplaintStatusUpdateRequest();
        request.setStatus(ComplaintStatus.IN_PROGRESS);
        request.setRemark("Technician has been assigned.");

        ComplaintResponse response = complaintService.updateStatus(5L, request);

        assertThat(response.getStatus()).isEqualTo(ComplaintStatus.IN_PROGRESS);
        assertThat(response.getRemarks()).hasSize(1);
        assertThat(response.getRemarks().get(0).getRemark()).isEqualTo("Technician has been assigned.");
    }

    @Test
    void updateStatus_throwsResourceNotFoundException_whenComplaintMissing() {
        when(complaintRepository.findById(999L)).thenReturn(Optional.empty());

        ComplaintStatusUpdateRequest request = new ComplaintStatusUpdateRequest();
        request.setStatus(ComplaintStatus.RESOLVED);
        request.setRemark("Fixed.");

        assertThatThrownBy(() -> complaintService.updateStatus(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
