package com.electricity.billing.service;

import com.electricity.billing.dto.request.AddBillRequest;
import com.electricity.billing.dto.request.RegisterRequest;
import com.electricity.billing.entity.enums.CustomerType;
import com.electricity.billing.entity.enums.ElectricalSection;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Bean Validation tests for registration and bill request DTOs (US001, US015 field rules). */
class RequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
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
    void registerRequest_hasNoViolations_whenAllFieldsValid() {
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRegisterRequest());
        assertThat(violations).isEmpty();
    }

    @Test
    void registerRequest_rejectsConsumerNumber_whenNot13Digits() {
        RegisterRequest request = validRegisterRequest();
        request.setConsumerNumber("12345");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("consumerNumber"));
    }

    @Test
    void registerRequest_rejectsWeakPassword_whenComplexityRulesNotMet() {
        RegisterRequest request = validRegisterRequest();
        request.setPassword("weakpass");
        request.setConfirmPassword("weakpass");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void registerRequest_rejectsMismatchedConfirmPassword() {
        RegisterRequest request = validRegisterRequest();
        request.setConfirmPassword("Different1!");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("confirmPassword")
                && v.getMessage().contains("do not match"));
    }

    @Test
    void registerRequest_rejectsInvalidEmailFormat() {
        RegisterRequest request = validRegisterRequest();
        request.setEmail("not-an-email");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void registerRequest_rejectsInvalidMobileNumber() {
        RegisterRequest request = validRegisterRequest();
        request.setMobileNumber("12345");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("mobileNumber"));
    }

    @Test
    void addBillRequest_rejectsNegativeBillAmount() {
        AddBillRequest request = new AddBillRequest();
        request.setConsumerNumber("1234567890123");
        request.setBillingPeriod("JUL-2026");
        request.setBillDate(LocalDate.now());
        request.setDueDate(LocalDate.now().plusDays(20));
        request.setBillAmount(new BigDecimal("-100"));

        Set<ConstraintViolation<AddBillRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("billAmount"));
    }

    @Test
    void addBillRequest_rejectsDueDateBeforeBillDate() {
        AddBillRequest request = new AddBillRequest();
        request.setConsumerNumber("1234567890123");
        request.setBillingPeriod("JUL-2026");
        request.setBillDate(LocalDate.of(2026, 7, 20));
        request.setDueDate(LocalDate.of(2026, 7, 1));
        request.setBillAmount(new BigDecimal("500"));

        Set<ConstraintViolation<AddBillRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Due Date cannot be before Bill Date"));
    }

    @Test
    void addBillRequest_hasNoViolations_whenValid() {
        AddBillRequest request = new AddBillRequest();
        request.setConsumerNumber("1234567890123");
        request.setBillingPeriod("JUL-2026");
        request.setBillDate(LocalDate.of(2026, 7, 1));
        request.setDueDate(LocalDate.of(2026, 7, 20));
        request.setBillAmount(new BigDecimal("500"));

        Set<ConstraintViolation<AddBillRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }
}
