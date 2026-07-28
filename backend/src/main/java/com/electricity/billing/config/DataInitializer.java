package com.electricity.billing.config;

import com.electricity.billing.entity.*;
import com.electricity.billing.entity.enums.*;
import com.electricity.billing.repository.*;
import com.electricity.billing.util.IdGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds the SQLite database with sample users/customers/bills/complaints so the application
 * is immediately demoable after a fresh checkout. Runs only when the users table is empty.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ConsumerRepository consumerRepository;
    private final BillRepository billRepository;
    private final ComplaintRepository complaintRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Sample data already present, skipping seed.");
            return;
        }
        log.info("Seeding sample data...");

        User admin = userRepository.save(User.builder()
                .userId("admin1")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN)
                .enabled(true)
                .build());

        userRepository.save(User.builder()
                .userId("sme1")
                .password(passwordEncoder.encode("Sme@1234"))
                .role(Role.SME)
                .enabled(true)
                .build());

        Customer customer1 = createCustomer("john.doe", "John Doe", "123, MG Road, Bengaluru, Karnataka",
                "john.doe@example.com", "9876543210", CustomerType.RESIDENTIAL, ElectricalSection.REGION);
        Consumer consumer1 = createConsumer(customer1, "1234567890123");

        Customer customer2 = createCustomer("acme.traders", "Acme Traders Pvt Ltd", "45, Industrial Estate, Chennai, Tamil Nadu",
                "contact@acmetraders.example.com", "9123456789", CustomerType.COMMERCIAL, ElectricalSection.OFFICE);
        Consumer consumer2 = createConsumer(customer2, "9876543210987");

        createBill(consumer1, "MAY-2026", LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 25),
                new BigDecimal("1450.00"), BillStatus.PAID, LocalDate.of(2026, 5, 20));
        createBill(consumer1, "JUN-2026", LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 25),
                new BigDecimal("1620.50"), BillStatus.UNPAID, null);
        createBill(consumer1, "JUL-2026", LocalDate.of(2026, 7, 5), LocalDate.of(2026, 7, 25),
                new BigDecimal("1780.00"), BillStatus.UNPAID, null);

        createBill(consumer2, "JUN-2026", LocalDate.of(2026, 6, 3), LocalDate.of(2026, 6, 23),
                new BigDecimal("8420.00"), BillStatus.UNPAID, null);
        createBill(consumer2, "JUL-2026", LocalDate.of(2026, 7, 3), LocalDate.of(2026, 7, 23),
                new BigDecimal("9010.00"), BillStatus.UNPAID, null);

        Complaint complaint = Complaint.builder()
                .complaintNumber(IdGeneratorUtil.generateComplaintNumber())
                .consumer(consumer1)
                .complaintType(ComplaintType.POWER_OUTAGE)
                .category("Frequent Tripping")
                .description("Power trips every evening between 7 PM and 9 PM for the past week.")
                .preferredContactMethod(ContactMethod.PHONE)
                .contactDetails("9876543210")
                .status(ComplaintStatus.OPEN)
                .build();
        complaintRepository.save(complaint);

        log.info("Sample data seeded. Admin login: admin1 / Admin@123, SME login: sme1 / Sme@1234, " +
                "Customer login: {} / Welcome@1234", customer1.getUser().getUserId());
    }

    private Customer createCustomer(String userId, String fullName, String address, String email,
                                     String mobile, CustomerType type, ElectricalSection section) {
        User user = userRepository.save(User.builder()
                .userId(userId)
                .password(passwordEncoder.encode("Welcome@1234"))
                .role(Role.CUSTOMER)
                .enabled(true)
                .build());

        return customerRepository.save(Customer.builder()
                .customerCode(IdGeneratorUtil.generateCustomerCode())
                .fullName(fullName)
                .address(address)
                .email(email)
                .mobileNumber(mobile)
                .customerType(type)
                .electricalSection(section)
                .status(CustomerStatus.ACTIVE)
                .user(user)
                .build());
    }

    private Consumer createConsumer(Customer customer, String consumerNumber) {
        return consumerRepository.save(Consumer.builder()
                .consumerNumber(consumerNumber)
                .customer(customer)
                .connectionStatus(ConnectionStatus.CONNECTED)
                .build());
    }

    private void createBill(Consumer consumer, String period, LocalDate billDate, LocalDate dueDate,
                             BigDecimal amount, BillStatus status, LocalDate paymentDate) {
        billRepository.save(Bill.builder()
                .billNumber(IdGeneratorUtil.generateBillNumber())
                .consumer(consumer)
                .billingPeriod(period)
                .billDate(billDate)
                .dueDate(dueDate)
                .billAmount(amount)
                .lateFee(BigDecimal.ZERO)
                .status(status)
                .paymentDate(paymentDate)
                .build());
    }
}
