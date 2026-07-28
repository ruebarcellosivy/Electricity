package com.electricity.billing.service;

import com.electricity.billing.dto.request.AdminCreateCustomerRequest;
import com.electricity.billing.dto.request.UpdateCustomerRequest;
import com.electricity.billing.dto.response.CustomerResponse;
import com.electricity.billing.entity.Customer;
import com.electricity.billing.entity.User;
import com.electricity.billing.entity.enums.CustomerStatus;
import com.electricity.billing.entity.enums.CustomerType;
import com.electricity.billing.entity.enums.ElectricalSection;
import com.electricity.billing.entity.enums.Role;
import com.electricity.billing.exception.DuplicateRecordException;
import com.electricity.billing.exception.ResourceNotFoundException;
import com.electricity.billing.repository.BillRepository;
import com.electricity.billing.repository.ComplaintRepository;
import com.electricity.billing.repository.CustomerRepository;
import com.electricity.billing.repository.UserRepository;
import com.electricity.billing.serviceimpl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BillRepository billRepository;
    @Mock
    private ComplaintRepository complaintRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private AdminCreateCustomerRequest createRequest() {
        AdminCreateCustomerRequest request = new AdminCreateCustomerRequest();
        request.setFullName("Ravi Kumar");
        request.setAddress("Plot 12, Sector 5, Gurugram");
        request.setEmail("ravi.kumar@example.com");
        request.setMobileNumber("9988776655");
        request.setCustomerType(CustomerType.RESIDENTIAL);
        request.setElectricalSection(ElectricalSection.OFFICE);
        request.setUserId("ravikumar");
        return request;
    }

    @Test
    void createCustomer_succeeds_withDefaultPasswordAndMustChangeFlag() {
        AdminCreateCustomerRequest request = createRequest();
        when(userRepository.existsByUserId("ravikumar")).thenReturn(false);
        when(customerRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        CustomerResponse response = customerService.createCustomer(request);

        assertThat(response.getFullName()).isEqualTo("Ravi Kumar");
        assertThat(response.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
        verify(userRepository).save(argThat(u -> u.isMustChangePassword() && u.getRole() == Role.CUSTOMER));
    }

    @Test
    void createCustomer_throwsDuplicateRecordException_whenUserIdExists() {
        AdminCreateCustomerRequest request = createRequest();
        when(userRepository.existsByUserId("ravikumar")).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(request))
                .isInstanceOf(DuplicateRecordException.class)
                .hasMessageContaining("User ID already exists");
    }

    @Test
    void createCustomer_throwsDuplicateRecordException_whenEmailExists() {
        AdminCreateCustomerRequest request = createRequest();
        when(userRepository.existsByUserId("ravikumar")).thenReturn(false);
        when(customerRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(request))
                .isInstanceOf(DuplicateRecordException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void updateCustomer_updatesFields_whenCustomerExists() {
        User user = User.builder().userId("ravikumar").role(Role.CUSTOMER).build();
        Customer existing = Customer.builder().id(5L).customerCode("CUST1").fullName("Old Name")
                .address("Old Address").email("old@example.com").mobileNumber("9000000000")
                .customerType(CustomerType.RESIDENTIAL).electricalSection(ElectricalSection.OFFICE)
                .status(CustomerStatus.ACTIVE).user(user).build();
        when(customerRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(customerRepository.existsByEmailAndIdNot("new@example.com", 5L)).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateCustomerRequest request = new UpdateCustomerRequest();
        request.setFullName("New Name");
        request.setAddress("New Address, City");
        request.setEmail("new@example.com");
        request.setMobileNumber("9111111111");
        request.setCustomerType(CustomerType.COMMERCIAL);

        CustomerResponse response = customerService.updateCustomer(5L, request);

        assertThat(response.getFullName()).isEqualTo("New Name");
        assertThat(response.getEmail()).isEqualTo("new@example.com");
        assertThat(response.getCustomerType()).isEqualTo(CustomerType.COMMERCIAL);
    }

    @Test
    void updateCustomer_throwsResourceNotFoundException_whenCustomerMissing() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.updateCustomer(99L, new UpdateCustomerRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteCustomer_deactivatesCustomerAndDisablesUser() {
        User user = User.builder().userId("ravikumar").role(Role.CUSTOMER).enabled(true).build();
        Customer existing = Customer.builder().id(5L).customerCode("CUST1").status(CustomerStatus.ACTIVE)
                .user(user).build();
        when(customerRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        customerService.deleteCustomer(5L);

        assertThat(existing.getStatus()).isEqualTo(CustomerStatus.INACTIVE);
        assertThat(user.isEnabled()).isFalse();
    }
}
