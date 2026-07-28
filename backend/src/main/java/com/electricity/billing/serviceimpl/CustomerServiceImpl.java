package com.electricity.billing.serviceimpl;

import com.electricity.billing.dto.request.AdminCreateCustomerRequest;
import com.electricity.billing.dto.request.UpdateCustomerRequest;
import com.electricity.billing.dto.response.BillResponse;
import com.electricity.billing.dto.response.CustomerResponse;
import com.electricity.billing.dto.response.HomeSummaryResponse;
import com.electricity.billing.dto.response.PageResponse;
import com.electricity.billing.entity.Bill;
import com.electricity.billing.entity.Customer;
import com.electricity.billing.entity.User;
import com.electricity.billing.entity.enums.BillStatus;
import com.electricity.billing.entity.enums.ComplaintStatus;
import com.electricity.billing.entity.enums.ConnectionStatus;
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
import com.electricity.billing.service.CustomerService;
import com.electricity.billing.util.BillMapperUtil;
import com.electricity.billing.util.IdGeneratorUtil;
import com.electricity.billing.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final BillRepository billRepository;
    private final ComplaintRepository complaintRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public CustomerResponse createCustomer(AdminCreateCustomerRequest request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new DuplicateRecordException("User ID already exists. Please choose a different User ID.");
        }
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateRecordException("Email already exists.");
        }

        User user = userRepository.save(User.builder()
                .userId(request.getUserId())
                .password(passwordEncoder.encode(IdGeneratorUtil.generateDefaultPassword()))
                .role(Role.CUSTOMER)
                .mustChangePassword(true)
                .enabled(true)
                .build());

        Customer customer = customerRepository.save(Customer.builder()
                .customerCode(IdGeneratorUtil.generateCustomerCode())
                .fullName(request.getFullName())
                .address(request.getAddress())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .customerType(request.getCustomerType())
                .electricalSection(request.getElectricalSection())
                .status(CustomerStatus.ACTIVE)
                .user(user)
                .build());

        return toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<CustomerResponse> listCustomers(String search, ElectricalSection section, CustomerType type,
                                                         int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = customerRepository.search(section, type, (search == null || search.isBlank()) ? null : search, pageable)
                .map(this::toResponse);
        return PageResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public CustomerResponse getCustomer(Long id) {
        return toResponse(findCustomerOrThrow(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = findCustomerOrThrow(id);
        if (customerRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateRecordException("Email already exists.");
        }
        customer.setFullName(request.getFullName());
        customer.setAddress(request.getAddress());
        customer.setEmail(request.getEmail());
        customer.setMobileNumber(request.getMobileNumber());
        customer.setCustomerType(request.getCustomerType());
        return toResponse(customerRepository.save(customer));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCustomer(Long id) {
        Customer customer = findCustomerOrThrow(id);
        // Billing history must be retained, so removal deactivates the customer and disconnects
        // every associated consumer instead of deleting rows outright.
        customer.setStatus(CustomerStatus.INACTIVE);
        customer.getConsumers().forEach(c -> c.setConnectionStatus(ConnectionStatus.DISCONNECTED));
        customer.getUser().setEnabled(false);
        customerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getMyProfile() {
        return toResponse(currentCustomer());
    }

    @Override
    @Transactional(readOnly = true)
    public HomeSummaryResponse getHomeSummary() {
        Customer customer = currentCustomer();
        List<Bill> latest = billRepository.findTop1ByConsumer_Customer_IdOrderByBillDateDesc(customer.getId());
        BillResponse latestBill = latest.isEmpty() ? null : BillMapperUtil.toResponse(latest.get(0));
        long unpaidCount = billRepository.countByConsumer_Customer_IdAndStatus(customer.getId(), BillStatus.UNPAID);
        long openComplaints = complaintRepository.countByConsumer_Customer_IdAndStatusIn(customer.getId(),
                List.of(ComplaintStatus.OPEN, ComplaintStatus.IN_PROGRESS));

        return HomeSummaryResponse.builder()
                .profile(toResponse(customer))
                .latestBill(latestBill)
                .unpaidBillCount(unpaidCount)
                .openComplaintCount(openComplaints)
                .build();
    }

    private Customer currentCustomer() {
        String userId = SecurityUtil.currentUserId();
        return customerRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for the current user."));
    }

    private Customer findCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    private CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .customerCode(customer.getCustomerCode())
                .fullName(customer.getFullName())
                .address(customer.getAddress())
                .email(customer.getEmail())
                .mobileNumber(customer.getMobileNumber())
                .customerType(customer.getCustomerType())
                .electricalSection(customer.getElectricalSection())
                .status(customer.getStatus())
                .userId(customer.getUser().getUserId())
                .consumerNumbers(customer.getConsumers().stream()
                        .map(c -> c.getConsumerNumber())
                        .collect(Collectors.toList()))
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
