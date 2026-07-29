package com.electricity.billing.serviceimpl;

import com.electricity.billing.dto.request.ChangePasswordRequest;
import com.electricity.billing.dto.request.LoginRequest;
import com.electricity.billing.dto.request.RegisterRequest;
import com.electricity.billing.dto.response.LoginResponse;
import com.electricity.billing.dto.response.MessageResponse;
import com.electricity.billing.dto.response.RegisterResponse;
import com.electricity.billing.entity.Consumer;
import com.electricity.billing.entity.Customer;
import com.electricity.billing.entity.User;
import com.electricity.billing.entity.enums.ConnectionStatus;
import com.electricity.billing.entity.enums.CustomerStatus;
import com.electricity.billing.entity.enums.Role;
import com.electricity.billing.exception.AuthenticationFailedException;
import com.electricity.billing.exception.DuplicateRecordException;
import com.electricity.billing.exception.InvalidRequestException;
import com.electricity.billing.repository.ConsumerRepository;
import com.electricity.billing.repository.CustomerRepository;
import com.electricity.billing.repository.UserRepository;
import com.electricity.billing.service.AuthService;
import com.electricity.billing.util.IdGeneratorUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ConsumerRepository consumerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateRecordException("An account with this email already exists.");
        }
        if (customerRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new DuplicateRecordException("An account with this mobile number already exists.");
        }

        long userCount = userRepository.count();
        String generatedUserId = "USR" + (userCount + 1);
        while (userRepository.existsByUserId(generatedUserId)) {
            userCount++;
            generatedUserId = "USR" + (userCount + 1);
        }


        User user = userRepository.save(User.builder()
                .userId(generatedUserId)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .mustChangePassword(false)
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

        String generatedConsumerNumber = IdGeneratorUtil.generateConsumerNumber();
        consumerRepository.save(Consumer.builder()
                .consumerNumber(generatedConsumerNumber)
                .customer(customer)
                .connectionStatus(ConnectionStatus.CONNECTED)
                .build());

        return RegisterResponse.builder()
                .userId(generatedUserId)
                .customerCode(customer.getCustomerCode())
                .consumerNumber(generatedConsumerNumber)
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .message("Registration successful.")
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUserId(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new AuthenticationFailedException("Invalid User ID or Password.");
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return buildLoginResponse(request.getUserId());
    }

    @Override
    public void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(httpRequest, httpResponse, authentication);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse currentSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationFailedException("No active session.");
        }
        return buildLoginResponse(authentication.getName());
    }

    @Override
    public MessageResponse changePassword(ChangePasswordRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid User ID or Password."));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidRequestException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
        return MessageResponse.of("Password changed successfully.");
    }

    private LoginResponse buildLoginResponse(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid User ID or Password."));

        String fullName = user.getUserId();
        String customerCode = null;
        if (user.getRole() == Role.CUSTOMER) {
            Customer customer = customerRepository.findByUser_UserId(userId).orElse(null);
            if (customer != null) {
                fullName = customer.getFullName();
                customerCode = customer.getCustomerCode();
            }
        }

        return LoginResponse.builder()
                .userId(user.getUserId())
                .role(user.getRole())
                .fullName(fullName)
                .customerCode(customerCode)
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }
}
