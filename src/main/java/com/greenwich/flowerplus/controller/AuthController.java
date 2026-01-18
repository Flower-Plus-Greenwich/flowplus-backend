package com.greenwich.flowerplus.controller;

import com.greenwich.flowerplus.common.ApiResult;
import com.greenwich.flowerplus.common.exception.TokenRefreshException;
import com.greenwich.flowerplus.common.utils.CookieUtils;
import com.greenwich.flowerplus.dto.request.LoginRequest;
import com.greenwich.flowerplus.dto.request.RegisterRequest;
import com.greenwich.flowerplus.dto.response.AuthResponse;
import com.greenwich.flowerplus.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Simplified authentication controller.
 * Handles only: login, register, logout, and token refresh.
 * 
 * REMOVED endpoints:
 * - /otp/request, /otp/verify
 * - /google-login
 * - /forgot-password, /reset-password
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    private final AuthService authService;
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    /**
     * Register a new user.
     * Fields: firstName, lastName, email, password, confirmPassword
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResult<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {

        AuthResponse authResult = authService.register(request);
        setRefreshTokenCookie(response, authResult.refreshToken());
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResult.success(authResult, "Registration completed successfully!"));
    }

    /**
     * Login with email and password.
     * Single-step login, no OTP.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResult<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        log.info("Login attempt for user: {}", request.getEmail());
        AuthResponse authResult = authService.login(request);
        setRefreshTokenCookie(response, authResult.refreshToken());
        
        return ResponseEntity.ok(ApiResult.success(authResult, "Successfully logged in."));
    }

    /**
     * Refresh access token using refresh token from cookie.
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResult<AuthResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        String rawRefreshToken = CookieUtils.getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);

        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new TokenRefreshException("", "Refresh token is missing");
        }

        AuthResponse authResult = authService.refreshToken(rawRefreshToken);
        setRefreshTokenCookie(response, authResult.refreshToken());
        
        return ResponseEntity.ok(ApiResult.success(authResult, "Token refreshed successfully."));
    }

    /**
     * Logout user and invalidate tokens.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        String rawRefreshToken = CookieUtils.getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String accessToken = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        authService.logout(accessToken, rawRefreshToken);

        // Clear refresh token cookie
        ResponseCookie cleared = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(0)
            .sameSite("Strict")
            .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cleared.toString());

        return ResponseEntity.noContent().build();
    }

    /**
     * Set refresh token as HTTP-only cookie.
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
            .httpOnly(true)
            .secure(false) // Set to true in production with HTTPS
            .path("/")
            .maxAge(Duration.ofDays(7))
            .sameSite("Strict")
            .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
