package com.greenwich.flowerplus.common.utils;

import com.nimbusds.jose.jwk.RSAKey;
import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeyUtils {

    @Value("${jwt.key.private-path}")
    private String privateKeyPath;

    @Value("${jwt.key.public-path}")
    private String publicKeyPath;

    @Value("${jwt.key.id}")
    private String keyId;

    public RSAKey getRsaKey() {
        try {
            // Read Private Key
            String privateKeyContent = readKeyFile(privateKeyPath)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);

            // Read Public Key
            String publicKeyContent = readKeyFile(publicKeyPath)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

            // Create Nimbus RSAKey
            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(keyId)
                    .build();

        } catch (Exception e) {
            log.error("Error reading key file", e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Error reading Key file: " + e.getMessage());
        }
    }

    private String readKeyFile(String path) throws IOException {
        String resourcePath = path;
        if (path.startsWith("classpath:")) {
            resourcePath = path.substring(10);
        }
        return new String(new ClassPathResource(resourcePath).getInputStream().readAllBytes());
    }
}