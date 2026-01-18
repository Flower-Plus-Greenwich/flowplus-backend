package com.greenwich.flowerplus.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.greenwich.flowerplus.common.ApiResult;
import com.greenwich.flowerplus.common.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Trả về 401 (Chuẩn hơn 403 cho lỗi chưa đăng nhập)
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String traceId = java.util.UUID.randomUUID().toString();
        ApiResult<?> errorResponse = ApiResult.error(ErrorCode.UNAUTHENTICATED.getCode(), ErrorCode.UNAUTHENTICATED.getMessage(), traceId);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
