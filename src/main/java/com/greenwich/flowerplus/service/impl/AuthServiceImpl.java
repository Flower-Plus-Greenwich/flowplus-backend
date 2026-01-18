package com.greenwich.flowerplus.service.impl;

import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.common.enums.IdentityProvider;
import com.greenwich.flowerplus.common.enums.UserStatus;
import com.greenwich.flowerplus.common.exception.AppException;
import com.greenwich.flowerplus.dto.request.LoginRequest;
import com.greenwich.flowerplus.dto.request.RegisterRequest;
import com.greenwich.flowerplus.dto.response.AuthResponse;
import com.greenwich.flowerplus.entity.RefreshToken;
import com.greenwich.flowerplus.entity.UserAccount;
import com.greenwich.flowerplus.entity.UserProfile;
import com.greenwich.flowerplus.repository.UserAccountRepository;
import com.greenwich.flowerplus.repository.UserProfileRepository;
import com.greenwich.flowerplus.service.AuthService;
import com.greenwich.flowerplus.service.RefreshTokenService;
import com.greenwich.flowerplus.service.TokenBlacklistService;
import com.greenwich.flowerplus.service.TokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Simplified authentication service implementation.
 * Handles basic login, register, logout, and token refresh.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate with email and password
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
            authenticationManager.authenticate(authToken);

            // Get user from database
            UserAccount user = userAccountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

            // Generate tokens
            String accessToken = tokenService.generateAccessToken(user);
            String refreshToken = tokenService.generateRefreshToken();
            refreshTokenService.saveRefreshToken(user, refreshToken);

            log.info("User {} logged in successfully", request.getEmail());

            return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        } catch (DisabledException e) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        } catch (LockedException e) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        } catch (BadCredentialsException e) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }

        // Check if email already exists
        if (userAccountRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // Create user account
        UserAccount user = UserAccount.builder()
            .email(request.getEmail())
            .username(generateUsername(request.getEmail()))
            .password(passwordEncoder.encode(request.getPassword()))
            .status(UserStatus.ACTIVE)
            .provider(IdentityProvider.LOCAL)
            .build();
            
        // NO Role assignment

        user = userAccountRepository.save(user);

        // Create user profile with name
        String fullName = (request.getFirstName() + " " + request.getLastName()).trim();
        UserProfile profile = UserProfile.builder()
            .userId(user.getId())
            .fullName(fullName)
            .build();
        userProfileRepository.save(profile);

        // Generate tokens
        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken();
        refreshTokenService.saveRefreshToken(user, refreshToken);

        log.info("User {} registered successfully", request.getEmail());

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    @Override
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        // Revoke Access Token (Blacklist)
        if (accessToken != null && !accessToken.isBlank()) {
            long remainingSeconds = tokenService.getRemainingTimeInSeconds(accessToken);
            if (remainingSeconds > 0) {
                tokenBlacklistService.blacklistToken(accessToken, remainingSeconds);
                log.info("Access token blacklisted. TTL: {}s", remainingSeconds);
            }
        }

        // Revoke Refresh Token
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revokeRefreshToken(refreshToken);
            log.info("User logged out, refresh token revoked");
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        if (refreshTokenStr == null || refreshTokenStr.isBlank()) {
            throw new AppException(ErrorCode.TOKEN_INVALID);
        }

        // Verify refresh token
        RefreshToken currentToken = refreshTokenService.verifyRefreshToken(refreshTokenStr);
        if (currentToken == null) {
            throw new AppException(ErrorCode.TOKEN_INVALID);
        }

        UserAccount user = currentToken.getUser();

        // Generate new access token
        String newAccessToken = tokenService.generateAccessToken(user);

        // Check if refresh token needs rotation (expires within 3 days)
        String finalRefreshToken = refreshTokenStr;
        long daysUntilExpiry = Duration.between(Instant.now(), currentToken.getExpiryDate()).toDays();
        
        if (daysUntilExpiry <= 3) {
            // Rotate refresh token
            String newRefreshToken = tokenService.generateRefreshToken();
            refreshTokenService.revokeRefreshToken(refreshTokenStr);
            refreshTokenService.saveRefreshToken(user, newRefreshToken);
            finalRefreshToken = newRefreshToken;
        }

        return AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(finalRefreshToken)
            .build();
    }

    /**
     * Generate a unique username from email.
     */
    private String generateUsername(String email) {
        String base = email.split("@")[0]
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase();

        if (base.length() < 3) {
            base = "user" + base;
        }

        String candidate = base;
        if (!userAccountRepository.existsByUsername(candidate)) {
            return candidate;
        }

        // Add random suffix if username exists
        for (int i = 0; i < 5; i++) {
            String suffix = String.valueOf(System.nanoTime());
            candidate = base + "_" + suffix.substring(suffix.length() - 5);
            if (!userAccountRepository.existsByUsername(candidate)) {
                return candidate;
            }
        }

        return base + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
