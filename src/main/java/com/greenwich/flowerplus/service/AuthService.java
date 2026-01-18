package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.dto.request.LoginRequest;
import com.greenwich.flowerplus.dto.request.RegisterRequest;
import com.greenwich.flowerplus.dto.response.AuthResponse;

/**
 * Simplified authentication service interface.
 * Handles basic login, register, logout, and token refresh.
 * Does NOT handle roles, permissions, or authorization logic.
 */
public interface AuthService {

    /**
     * Authenticate user with email and password.
     * Single-step login, no OTP.
     */
    AuthResponse login(LoginRequest request);

    /**
     * Register a new user account.
     * Basic validation only (required fields, password match).
     * No email verification.
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Logout user by revoking refresh token.
     */
    void logout(String accessToken, String refreshToken);

    /**
     * Refresh access token using refresh token.
     */
    AuthResponse refreshToken(String refreshToken);
}
