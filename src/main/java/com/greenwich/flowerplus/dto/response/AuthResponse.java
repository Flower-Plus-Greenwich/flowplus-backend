package com.greenwich.flowerplus.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

/**
 * Response DTO for authentication operations.
 * Contains access token and refresh token.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String accessToken,
        String refreshToken
) {}
