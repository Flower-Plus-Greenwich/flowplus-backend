package com.greenwich.flowerplus.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for user registration.
 * Contains only essential fields: firstName, lastName, email, password, confirmPassword
 */
@Getter
@Setter
public class RegisterRequest {

    @Schema(description = "User's first name", example = "John")
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not accept more than 50 characters")
    private String firstName;

    @Schema(description = "User's last name", example = "Doe")
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not accept more than 50 characters")
    private String lastName;

    @Schema(description = "Email address", example = "john.doe@example.com")
    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Email should be valid")
    @Size(max = 255, message = "Email must not accept more than 255 characters")
    private String email;

    @Schema(description = "Password (min 8 chars)", example = "Password@123")
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @Schema(description = "Confirm Password (must match password)", example = "Password@123")
    @NotBlank(message = "Confirm Password is required")
    private String confirmPassword;
}
