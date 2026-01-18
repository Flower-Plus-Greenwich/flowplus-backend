package com.greenwich.flowerplus.controller;

import com.greenwich.flowerplus.common.utils.KeyUtils;
import com.nimbusds.jose.jwk.JWKSet;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class JwksController {

    private final KeyUtils keyUtils;

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> keys() {
        return new JWKSet(keyUtils.getRsaKey().toPublicJWK()).toJSONObject();
    }


}
