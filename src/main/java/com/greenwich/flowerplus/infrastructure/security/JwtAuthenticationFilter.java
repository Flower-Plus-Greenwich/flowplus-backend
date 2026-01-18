package com.greenwich.flowerplus.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenwich.flowerplus.common.ApiResult;
import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.common.exception.TokenExpiredException;
import com.greenwich.flowerplus.service.TokenBlacklistService;
import com.greenwich.flowerplus.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService jwtTokenService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String token;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        token = authHeader.substring(7);

        // 1. CHECK BLACKLIST
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            log.warn("Blacklisted token attempt: {}", token);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, ErrorCode.TOKEN_INVALID);
            return;
        }

        // 2. CHECK TOKEN & EXTRACT USERNAME
        try {
            username = jwtTokenService.extractUsername(token);
        } catch (TokenExpiredException ex) {
            log.warn("JWT expired: {}", ex.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, ErrorCode.TOKEN_EXPIRED);
            return;

        } catch (Exception ex) {
            log.warn("Invalid JWT: {}", ex.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, ErrorCode.TOKEN_INVALID);
            return;
        }

        // 3. AUTHENTICATION LOGIC
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Check cache or load from DB. Here we load from DB.
            // username is likely ID (TSID)
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtTokenService.isValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, ErrorCode errorCode, Object... args) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String traceId = java.util.UUID.randomUUID().toString();
        ApiResult<?> apiResult = ApiResult.error(errorCode.getCode(), errorCode.getMessage(), traceId);
        String jsonResponse = objectMapper.writeValueAsString(apiResult);
        response.getWriter().write(jsonResponse);
    }
}