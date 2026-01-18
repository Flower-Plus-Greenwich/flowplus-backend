package com.greenwich.flowerplus.service.impl;

import com.greenwich.flowerplus.common.exception.TokenErrorException;
import com.greenwich.flowerplus.common.utils.KeyUtils;
import com.greenwich.flowerplus.entity.UserAccount;
import com.greenwich.flowerplus.service.TokenService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final KeyUtils keyUtils;
    @Value("${jwt.signerKey:default-signer-key-that-is-very-long-and-secure-at-least-32-bytes}")
    private String signerKey;

    @Override
    public String generateAccessToken(Authentication authentication) {
        RSAKey rsaKey = keyUtils.getRsaKey();
        Instant now = Instant.now();

        // Header
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(rsaKey.getKeyID())
                .type(JOSEObjectType.JWT)
                .build();

        // Payload (Claims) - REMOVED ROLES
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(authentication.getName())
                .issuer("flowerplus-auth-service")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(1, ChronoUnit.HOURS)))
                .build();

        // Sign
        return generateSignedJwt(rsaKey, header, claims);
    }

    @Override
    public String generateAccessToken(UserAccount userAccount) {
        RSAKey rsaKey = keyUtils.getRsaKey();
        Instant now = Instant.now();

        // Header
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(rsaKey.getKeyID())
                .type(JOSEObjectType.JWT)
                .build();

        // Payload (Claims) - REMOVED ROLES
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(userAccount.getId()))
                .claim("userId", userAccount.getId())
                .claim("email", userAccount.getEmail())
                .issuer("flowerplus-auth-service")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(1, ChronoUnit.HOURS)))
                .build();

        // Sign
        return generateSignedJwt(rsaKey, header, claims);
    }

    private String generateSignedJwt(RSAKey rsaKey, JWSHeader header, JWTClaimsSet claims) {
        SignedJWT signedJWT = new SignedJWT(header, claims);
        try {
            JWSSigner signer = new RSASSASigner(rsaKey.toPrivateKey());
            signedJWT.sign(signer);
        } catch (JOSEException e) {
            throw new TokenErrorException(e.getMessage());
        }

        return signedJWT.serialize();
    }

    @Override
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    private Date extractExpiration(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);
        RSAKey rsaKey = keyUtils.getRsaKey();

        JWSVerifier verifier = new RSASSAVerifier(rsaKey.toRSAPublicKey());
        if (!signedJWT.verify(verifier)) {
            throw new TokenErrorException("Invalid token signature");
        }
        return signedJWT.getJWTClaimsSet().getExpirationTime();
    }

    @Override
    public long getRemainingTimeInSeconds(String token) {
        try {
            Date expiration = extractExpiration(token);
            long diff = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, diff / 1000);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public boolean isValid(String token, UserDetails userDetails) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            RSAKey rsaKey = keyUtils.getRsaKey();

            JWSVerifier verifier = new RSASSAVerifier(rsaKey.toRSAPublicKey());
            if (!signedJWT.verify(verifier)) {
                log.error("isValid failed: Invalid Signature");
                return false;
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Date expirationTime = claims.getExpirationTime();
            String username = claims.getSubject(); // This is userId (TSID)
            
            // Allow matching against Username OR ID if UserDetails allows
            // userDetails.getUsername() currently returns... let's check SecurityUserDetails later.
            // Usually it returns the identifier used to load.
            
            String dbUsername = userDetails.getUsername(); // This might be email or ID depending on how it was loaded.
             
            // Refined check: userDetails might return email, but token subject is ID.
            // Or vice versa.
            // Previous code: boolean isUsernameMatch = username.equals(dbUsername);
            // I should ensure consistency. 
            // In generateAccessToken(UserAccount), subject is userAccount.getId().
            // In UserDetailsServiceImpl, if input is numeric, it loads by ID.
            
            boolean isUsernameMatch = username.equals(dbUsername);
            
            // Also check against email if ID doesn't match, or vice versa if we can. 
            // But for now, let's keep it simple. If subject is ID, userDetails.getUsername() should be ID.
            // UserDetailsServiceImpl sets username to userAccount.getId() usually? 
            // Actually UserDetails usually returns username/email. 
            // I will check SecurityUserDetails.
            
            if (!isUsernameMatch) log.debug("isValid failed: Subject mismatch. Token: {}, User: {}", username, dbUsername);

            return (expirationTime != null && expirationTime.after(new Date()) && isUsernameMatch);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String extractUsername(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            RSAKey rsaKey = keyUtils.getRsaKey();
            JWSVerifier verifier = new RSASSAVerifier(rsaKey.toRSAPublicKey());

            if (!signedJWT.verify(verifier)) {
                throw new TokenErrorException("Invalid token signature");
            }
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            throw new TokenErrorException("Failed to extract username: " + e.getMessage());
        }
    }
}
