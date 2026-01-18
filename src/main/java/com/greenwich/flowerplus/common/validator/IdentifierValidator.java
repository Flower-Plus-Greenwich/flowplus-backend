package com.greenwich.flowerplus.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class IdentifierValidator implements ConstraintValidator<ValidIdentifier, String> {

    // Regex Username: Ví dụ: Chữ cái, số, gạch dưới, từ 3-20 ký tự
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9._]{3,20}$";

    // Regex Email: (Cơ bản)
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return false; // Không được để trống (hoặc trả true nếu dùng @NotNull ở DTO)
        }

        // Logic OR: Hoặc là Email, Hoặc là Username
        boolean isEmail = Pattern.matches(EMAIL_PATTERN, value);
        boolean isUsername = Pattern.matches(USERNAME_PATTERN, value);

        return isEmail || isUsername;
    }
}