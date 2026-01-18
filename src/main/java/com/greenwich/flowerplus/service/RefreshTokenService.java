package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.entity.RefreshToken;
import com.greenwich.flowerplus.entity.UserAccount;

/**
 * Service for managing refresh tokens.
 */
public interface RefreshTokenService {
    RefreshToken saveRefreshToken(UserAccount user, String tokenString);
    RefreshToken verifyRefreshToken(String token);
    void revokeRefreshToken(String token);
    void deleteByToken(String token);
    void revokeAllUserTokens(UserAccount user);
    void forceDeleteTokenByUserId(Long userId);
}
