package com.electricity.billing.service;

import com.electricity.billing.dto.request.ChangePasswordRequest;
import com.electricity.billing.dto.request.LoginRequest;
import com.electricity.billing.dto.request.RegisterRequest;
import com.electricity.billing.dto.response.LoginResponse;
import com.electricity.billing.dto.response.RegisterResponse;
import com.electricity.billing.entity.Customer;
import com.electricity.billing.entity.User;
import com.electricity.billing.entity.enums.CustomerType;
import com.electricity.billing.entity.enums.ElectricalSection;
import com.electricity.billing.entity.enums.Role;
import com.electricity.billing.exception.AuthenticationFailedException;
import com.electricity.billing.exception.DuplicateRecordException;
import com.electricity.billing.exception.InvalidRequestException;
import com.electricity.billing.repository.ConsumerRepository;
import com.electricity.billing.repository.CustomerRepository;
import com.electricity.billing.repository.UserRepository;
import com.electricity.billing.serviceimpl.AuthServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ConsumerRepository consumerRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private SecurityContextRepository securityContextRepository;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private HttpServletResponse httpServletResponse;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, customerRepository, consumerRepository,
                passwordEncoder, authenticationManager, securityContextRepository);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private RegisterRequest validRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setConsumerNumber("1234567890123");
        request.setFullName("Jane Smith");
        request.setAddress("221B Baker Street, London");
        request.setEmail("jane.smith@example.com");
        request.setMobileNumber("9876543210");
        request.setCustomerType(CustomerType.RESIDENTIAL);
        request.setElectricalSection(ElectricalSection.REGION);
        request.setUserId("janesmith");
        request.setPassword("Passw0rd!");
        request.setConfirmPassword("Passw0rd!");
        return request;
    }

    @Test
    void register_savesUserCustomerAndConsumer_whenDataIsValid() {
        RegisterRequest request = validRegisterRequest();
        when(userRepository.existsByUserId(request.getUserId())).thenReturn(false);
        when(customerRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(consumerRepository.existsByConsumerNumber(request.getConsumerNumber())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        RegisterResponse response = authService.register(request);

        assertThat(response.getFullName()).isEqualTo("Jane Smith");
        assertThat(response.getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(response.getCustomerCode()).startsWith("CUST");
        verify(consumerRepository).save(any());
    }

    @Test
    void register_throwsDuplicateRecordException_whenUserIdAlreadyExists() {
        RegisterRequest request = validRegisterRequest();
        when(userRepository.existsByUserId(request.getUserId())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateRecordException.class)
                .hasMessageContaining("User ID already exists");

        verify(customerRepository, never()).save(any());
    }

    @Test
    void register_throwsDuplicateRecordException_whenConsumerNumberAlreadyRegistered() {
        RegisterRequest request = validRegisterRequest();
        when(userRepository.existsByUserId(request.getUserId())).thenReturn(false);
        when(customerRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(consumerRepository.existsByConsumerNumber(request.getConsumerNumber())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateRecordException.class)
                .hasMessageContaining("Consumer Number");
    }

    @Test
    void login_returnsLoginResponse_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setUserId("janesmith");
        request.setPassword("Passw0rd!");

        Authentication authentication = new UsernamePasswordAuthenticationToken("janesmith", "Passw0rd!",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        User user = User.builder().userId("janesmith").role(Role.CUSTOMER).enabled(true).mustChangePassword(false).build();
        when(userRepository.findByUserId("janesmith")).thenReturn(Optional.of(user));
        Customer customer = Customer.builder().customerCode("CUST123").fullName("Jane Smith").user(user).build();
        when(customerRepository.findByUser_UserId("janesmith")).thenReturn(Optional.of(customer));

        LoginResponse response = authService.login(request, httpServletRequest, httpServletResponse);

        assertThat(response.getUserId()).isEqualTo("janesmith");
        assertThat(response.getFullName()).isEqualTo("Jane Smith");
        assertThat(response.getRole()).isEqualTo(Role.CUSTOMER);
        verify(securityContextRepository).saveContext(any(), eq(httpServletRequest), eq(httpServletResponse));
    }

    @Test
    void login_throwsAuthenticationFailedException_whenCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest();
        request.setUserId("janesmith");
        request.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request, httpServletRequest, httpServletResponse))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("Invalid User ID or Password");
    }

    @Test
    void changePassword_updatesPassword_whenOldPasswordMatches() {
        User user = User.builder().userId("janesmith").role(Role.CUSTOMER)
                .password(passwordEncoder.encode("OldPass1!")).mustChangePassword(true).build();
        setAuthenticatedUser("janesmith");
        when(userRepository.findByUserId("janesmith")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("OldPass1!");
        request.setPassword("NewPass1!");
        request.setConfirmPassword("NewPass1!");

        var response = authService.changePassword(request);

        assertThat(response.getMessage()).contains("changed successfully");
        assertThat(user.isMustChangePassword()).isFalse();
    }

    @Test
    void changePassword_throwsInvalidRequestException_whenOldPasswordIncorrect() {
        User user = User.builder().userId("janesmith").role(Role.CUSTOMER)
                .password(passwordEncoder.encode("OldPass1!")).build();
        setAuthenticatedUser("janesmith");
        when(userRepository.findByUserId("janesmith")).thenReturn(Optional.of(user));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("WrongOld!");
        request.setPassword("NewPass1!");
        request.setConfirmPassword("NewPass1!");

        assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Current password is incorrect");
    }

    private void setAuthenticatedUser(String userId) {
        var authentication = new UsernamePasswordAuthenticationToken(userId, null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}
