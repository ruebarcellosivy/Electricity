package com.electricity.billing.service;

import com.electricity.billing.dto.request.ChangePasswordRequest;
import com.electricity.billing.dto.request.LoginRequest;
import com.electricity.billing.dto.request.RegisterRequest;
import com.electricity.billing.dto.response.LoginResponse;
import com.electricity.billing.dto.response.MessageResponse;
import com.electricity.billing.dto.response.RegisterResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    LoginResponse currentSession();

    MessageResponse changePassword(ChangePasswordRequest request);
}
