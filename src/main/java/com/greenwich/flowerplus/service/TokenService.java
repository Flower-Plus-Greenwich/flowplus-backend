package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.entity.UserAccount;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Token service interface for JWT token operations.
 */
public interface TokenService {
    String generateAccessToken(Authentication authentication);
    String generateAccessToken(UserAccount userAccount);
    String generateRefreshToken();
    long getRemainingTimeInSeconds(String token);
    boolean isValid(String token, UserDetails userDetails);
    String extractUsername(String token);
}
