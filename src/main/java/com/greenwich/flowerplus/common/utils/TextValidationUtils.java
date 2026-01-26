package com.greenwich.flowerplus.common.utils;

import com.greenwich.flowerplus.common.constant.BlockWords;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Utility class for text validation including bad words filtering,
 * HTML/script injection prevention, and text normalization.
 */
public final class TextValidationUtils {

    private TextValidationUtils() {
        // Utility class - prevent instantiation
    }

    // Pattern to detect HTML tags
    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]*>");
    
    // Pattern to detect script tags specifically
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    // Pattern to detect SQL injection attempts
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(--|;|'|\"|\\b(SELECT|INSERT|UPDATE|DELETE|DROP|UNION|ALTER|CREATE|TRUNCATE)\\b)",
            Pattern.CASE_INSENSITIVE
    );

    // Pattern for valid product/category name (letters, numbers, Vietnamese, spaces, basic punctuation)
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile(
            "^[\\p{L}\\p{N}\\s\\-_.,&()]+$",
            Pattern.UNICODE_CHARACTER_CLASS
    );

    /**
     * Check if text contains any blocked/bad words.
     * 
     * @param text The text to check
     * @return true if text contains bad words, false otherwise
     */
    public static boolean containsBadWords(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        
        String normalizedText = text.toLowerCase().trim();
        return Arrays.stream(BlockWords.getBadWords())
                .anyMatch(badWord -> normalizedText.contains(badWord.toLowerCase()));
    }

    /**
     * Find the first bad word in the text.
     *
     * @param text The text to check
     * @return The bad word found, or null if none
     */
    public static String findBadWord(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }

        String normalizedText = text.toLowerCase().trim();
        return Arrays.stream(BlockWords.getBadWords())
                .filter(badWord -> normalizedText.contains(badWord.toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if text contains potential HTML injection.
     *
     * @param text The text to check
     * @return true if HTML tags detected
     */
    public static boolean containsHtmlTags(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        return HTML_PATTERN.matcher(text).find();
    }

    /**
     * Check if text contains script tags (security risk).
     *
     * @param text The text to check
     * @return true if script tags detected
     */
    public static boolean containsScriptTags(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        return SCRIPT_PATTERN.matcher(text).find();
    }

    /**
     * Check if text contains potential SQL injection patterns.
     *
     * @param text The text to check
     * @return true if SQL injection patterns detected
     */
    public static boolean containsSqlInjection(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(text).find();
    }

    /**
     * Validate if text is a valid name (product, category, etc.).
     * Allows letters (including Vietnamese), numbers, spaces, and basic punctuation.
     *
     * @param text The text to validate
     * @return true if valid name format
     */
    public static boolean isValidNameFormat(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        return VALID_NAME_PATTERN.matcher(text.trim()).matches();
    }

    /**
     * Comprehensive validation for product/category names.
     * Checks for: bad words, HTML injection, SQL injection, valid format.
     *
     * @param name The name to validate
     * @return ValidationResult with status and error message if invalid
     */
    public static ValidationResult validateName(String name) {
        if (!StringUtils.hasText(name)) {
            return ValidationResult.failure("Name cannot be empty");
        }

        String trimmedName = name.trim();

        if (trimmedName.length() < 2) {
            return ValidationResult.failure("Name must be at least 2 characters");
        }

        if (trimmedName.length() > 255) {
            return ValidationResult.failure("Name cannot exceed 255 characters");
        }

        String badWord = findBadWord(trimmedName);
        if (badWord != null) {
            return ValidationResult.failure("Name contains inappropriate word: " + badWord);
        }

        if (containsHtmlTags(trimmedName)) {
            return ValidationResult.failure("Name cannot contain HTML tags");
        }

        if (containsSqlInjection(trimmedName)) {
            return ValidationResult.failure("Name contains invalid characters");
        }

        if (!isValidNameFormat(trimmedName)) {
            return ValidationResult.failure("Name contains invalid characters");
        }

        return ValidationResult.success();
    }

    /**
     * Validate description text (allows HTML for WYSIWYG but blocks scripts).
     *
     * @param description The description to validate
     * @return ValidationResult with status and error message if invalid
     */
    public static ValidationResult validateDescription(String description) {
        if (!StringUtils.hasText(description)) {
            return ValidationResult.success(); // Description can be empty
        }

        if (description.length() > 10000) {
            return ValidationResult.failure("Description cannot exceed 10000 characters");
        }

        String badWord = findBadWord(description);
        if (badWord != null) {
            return ValidationResult.failure("Description contains inappropriate word: " + badWord);
        }

        if (containsScriptTags(description)) {
            return ValidationResult.failure("Description cannot contain script tags");
        }

        return ValidationResult.success();
    }

    /**
     * Validate URL format (for assets).
     *
     * @param url The URL to validate
     * @return true if valid URL format
     */
    public static boolean isValidUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        
        try {
            // Basic URL validation
            return url.startsWith("http://") || url.startsWith("https://");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sanitize text by removing potentially dangerous content.
     *
     * @param text The text to sanitize
     * @return Sanitized text
     */
    public static String sanitize(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        
        // Remove script tags
        String result = SCRIPT_PATTERN.matcher(text).replaceAll("");
        
        // Trim whitespace
        return result.trim();
    }

    /**
     * Result class for validation operations.
     */
    public record ValidationResult(boolean isValid, String errorMessage) {
        
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message);
        }
    }
}
