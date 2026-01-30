package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.common.exception.AppException;
import com.greenwich.flowerplus.dto.request.LoginRequest;
import com.greenwich.flowerplus.dto.request.RegisterRequest;
import com.greenwich.flowerplus.dto.response.AuthResponse;
import com.greenwich.flowerplus.entity.RefreshToken;
import com.greenwich.flowerplus.entity.Role;
import com.greenwich.flowerplus.entity.UserAccount;
import com.greenwich.flowerplus.repository.RefreshTokenRepository;
import com.greenwich.flowerplus.repository.RoleRepository;
import com.greenwich.flowerplus.repository.UserAccountRepository;
import com.greenwich.flowerplus.repository.UserProfileRepository;
import com.greenwich.flowerplus.service.RefreshTokenService;
import com.greenwich.flowerplus.service.TokenBlacklistService;
import com.greenwich.flowerplus.service.TokenService;
import com.greenwich.flowerplus.service.impl.AuthServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenService tokenService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserAccount userAccount;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        //"John", "Doe", "john.doe@example.com", "Password@123", "Password@123"
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPassword("Password@123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setConfirmPassword("Password@123");


        loginRequest = new LoginRequest();
        //"john.doe@example.com", "Password@123"
        loginRequest.setEmail("john.doe");
        loginRequest.setPassword("Password@123");

        customerRole = Role.builder().id(1L).name("CUSTOMER").build();

        userAccount = UserAccount.builder()
            .email("john.doe@example.com")
            .username("johndoe")
            .password("encodedPassword")
            .userRoles(new HashSet<>())
            .build();
        userAccount.setId(1L);
    }

    @Test
    void register_Success() {
        // Arrange
        when(userAccountRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(userAccount);
        when(tokenService.generateAccessToken(any(UserAccount.class))).thenReturn("access-token");
        when(tokenService.generateRefreshToken()).thenReturn("refresh-token");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        verify(userProfileRepository, times(1)).save(any());
    }

    @Test
    void register_Fail_EmailExists() {
        when(userAccountRepository.existsByEmail(anyString())).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> authService.register(registerRequest));
        assertEquals(ErrorCode.USER_ALREADY_EXISTS.getCode(), exception.getErrorCode());
    }

    @Test
    void register_Fail_PasswordMismatch() {
        RegisterRequest badRequest = new RegisterRequest();

        AppException exception = assertThrows(AppException.class, () -> authService.register(badRequest));
        assertEquals(ErrorCode.INVALID_CONFIRM_PASSWORD.getCode(), exception.getErrorCode());
    }

    @Test
    void login_Success_NewRefreshToken() {
        // Arrange
        when(userAccountRepository.findByEmail(anyString())).thenReturn(Optional.of(userAccount));
        when(tokenService.generateAccessToken(any(UserAccount.class))).thenReturn("new-access-token");
        when(refreshTokenRepository.findValidTokenByUserId(anyLong())).thenReturn(Optional.empty()); // No valid token exists
        when(tokenService.generateRefreshToken()).thenReturn("new-refresh-token");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("new-access-token", response.accessToken());
        assertEquals("new-refresh-token", response.refreshToken());
        
        // Verify authentication manager was called
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_Success_ReuseRefreshToken() {
        // Arrange
        RefreshToken existingToken = RefreshToken.builder().token("existing-refresh-token").build();
        
        when(userAccountRepository.findByEmail(anyString())).thenReturn(Optional.of(userAccount));
        when(tokenService.generateAccessToken(any(UserAccount.class))).thenReturn("new-access-token");
        when(refreshTokenRepository.findValidTokenByUserId(anyLong())).thenReturn(Optional.of(existingToken)); // Valid token exists

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("new-access-token", response.accessToken());
        assertEquals("existing-refresh-token", response.refreshToken()); // Should reuse
        
        // Verify we DID NOT generate a new refresh token
        verify(tokenService, never()).generateRefreshToken();
    }

    @Test
    void login_Fail_BadCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad creds"));

        AppException exception = assertThrows(AppException.class, () -> authService.login(loginRequest));
        assertEquals(ErrorCode.INVALID_CREDENTIALS.getCode(), exception.getErrorCode());
    }
}
