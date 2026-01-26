package com.greenwich.flowerplus.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for user login.
 * Contains only email and password fields.
 */
@Getter
@Setter
public class LoginRequest {

    @Schema(description = "Email address", example = "owner@flowerplus.com")
    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Email should be valid")
    @Size(max = 255, message = "Email must not accept more than 255 characters")
    private String email;

    @Schema(description = "Password", example = "password123")
    @NotBlank(message = "Password is required")
    private String password;
}
