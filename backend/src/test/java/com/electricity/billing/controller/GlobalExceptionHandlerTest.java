package com.electricity.billing.controller;

import com.electricity.billing.dto.response.ErrorResponse;
import com.electricity.billing.exception.DuplicateRecordException;
import com.electricity.billing.exception.GlobalExceptionHandler;
import com.electricity.billing.exception.InvalidRequestException;
import com.electricity.billing.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Verifies that the GlobalExceptionHandler maps custom exceptions to the right HTTP status and message. */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void handlesResourceNotFoundException_with404() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(
                new ResourceNotFoundException("Bill not found with id: 5"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).contains("Bill not found");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
    }

    @Test
    void handlesDuplicateRecordException_with409() {
        ResponseEntity<ErrorResponse> response = handler.handleDuplicate(
                new DuplicateRecordException("User ID already exists."), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).contains("already exists");
    }

    @Test
    void handlesInvalidRequestException_with400() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidRequest(
                new InvalidRequestException("Due Date cannot be before Bill Date."), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("Due Date");
    }

    @Test
    void handlesGenericException_with500_andDoesNotLeakStackTrace() {
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(new RuntimeException("db is down"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).doesNotContain("db is down");
        assertThat(response.getBody().getMessage()).contains("unexpected error");
    }
}
