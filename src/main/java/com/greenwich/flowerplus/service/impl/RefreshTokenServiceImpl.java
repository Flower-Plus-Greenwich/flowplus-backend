package com.greenwich.flowerplus.service.impl;

import com.greenwich.flowerplus.common.exception.TokenRefreshException;
import com.greenwich.flowerplus.entity.RefreshToken;
import com.greenwich.flowerplus.entity.UserAccount;
import com.greenwich.flowerplus.repository.RefreshTokenRepository;
import com.greenwich.flowerplus.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh.expiration:2592000000}")
    private Long refreshTokenDurationMs;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefreshToken saveRefreshToken(UserAccount user, String tokenString) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(tokenString);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyRefreshToken(String rawToken) {
        RefreshToken token = refreshTokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new TokenRefreshException(rawToken, "Refresh token not found"));

        if (token.isRevoked()) {
            log.warn("SECURITY ALERT: Attempt to use revoked token: {}", rawToken);
            throw new TokenRefreshException(rawToken, "Refresh token was revoked");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(rawToken, "Refresh token was expired");
        }

        return token;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeRefreshToken(String rawToken) {
        refreshTokenRepository.findByToken(rawToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.info("User {} logged out (token revoked)", token.getUser().getEmail());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByToken(String rawToken) {
        refreshTokenRepository.findByToken(rawToken).ifPresent(refreshTokenRepository::delete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeAllUserTokens(UserAccount user) {
        refreshTokenRepository.revokeAllByUser(user);
        log.info("All refresh tokens revoked for user: {}", user.getEmail());
    }

    @Override
    public void forceDeleteTokenByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
