package com.greenwich.flowerplus.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ========================================================================
    // 1. SYSTEM & GENERAL ERRORS (Lỗi hệ thống & chung) - Prefix: SYS
    // ========================================================================
    // ========================================================================
    // 1. SYSTEM & GENERAL ERRORS (System & General) - Prefix: SYS
    // ========================================================================
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_001", "Internal server error"),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_002", "Database error occurred"), // Hide details for security
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_003", "File upload failed"),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_004", "Email send failed"),

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "SYS_005", "Invalid request"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "SYS_006", "Method not allowed"),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "SYS_007", "Rate limit exceeded"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "SYS_404", "Resource not found"), // Fallback for general 404
    INVALID_FILE(HttpStatus.BAD_REQUEST, "FILE_001", "Invalid file" ),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "FILE_002", "Invalid file type"),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_003" , "File delete failed" ),
    // ========================================================================
    // 2. AUTHENTICATION & SECURITY (Login & Security) - Prefix: AUTH
    // ========================================================================
    // 401: Not logged in or invalid token
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "AUTH_001", "Authentication required"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_002", "Invalid credentials"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_003", "Token expired"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_004", "Token invalid"),
    TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "AUTH_005", "Token revoked"),

    // 403: Logged in but missing permission
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_006", "Access denied"),

    // Custom Auth Logic
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "AUTH_007", "Account locked"),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "AUTH_008", "Account disabled"),
    ACCOUNT_NOT_VERIFIED(HttpStatus.FORBIDDEN, "AUTH_009", "Account not verified"),
    OAUTH2_PASSWORD_CHANGE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "AUTH_010", "OAuth2 password change not allowed"),
    ACCOUNT_BANNED(HttpStatus.FORBIDDEN, "AUTH_011", "Account banned"),

    OAUTH2_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_008", "OAuth2 error"),

    // ========================================================================
    // ACCOUNT STATUS LOGIC
    // ========================================================================

    // 1. NOT ACTIVATED
    USER_NOT_VERIFIED(HttpStatus.FORBIDDEN, "USER_010", "User not verified"),

    // 2. BANNED
    USER_BANNED(HttpStatus.FORBIDDEN, "USER_011", "User banned"),

    // 3. TEMPORARILY LOCKED
    USER_LOCKED(HttpStatus.FORBIDDEN, "USER_012", "User temporarily locked"),

    // 4. Registration Incomplete
    REGISTRATION_REQUIRED(HttpStatus.FORBIDDEN, "USER_013", "Registration incomplete"),


    // ========================================================================
    // 3. USER DOMAIN - Prefix: USER
    // ========================================================================
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "User not found"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_002", "User already exists"),

    // Validation Input
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "USER_003", "Password must be at least 10 characters long and include uppercase, lowercase, number, and special character"),
    INVALID_CONFIRM_PASSWORD(HttpStatus.BAD_REQUEST, "USER_004", "Password does not match with confirm password"),
    INVALID_DISPLAY_NAME(HttpStatus.BAD_REQUEST, "USER_004", "Display name contains forbidden words or exceeds length limit"),
    INVALID_DOB(HttpStatus.BAD_REQUEST, "USER_005", "Date of birth must be a past date"),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "USER_008", "Email must be a valid email address format"),
    INVALID_PHONE_FORMAT(HttpStatus.BAD_REQUEST, "USER_009", "Phone number must be a valid format"),
    INVALID_NAME_FORMAT(HttpStatus.BAD_REQUEST, "USER_010", "Name contains invalid characters or forbidden words"),

    // Logic Reset Password
    INVALID_RESET_TOKEN(HttpStatus.BAD_REQUEST, "USER_006", "Invalid reset token"),
    EMAIL_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "USER_007", "Email already verified"),

    // ========================================================================
    // 4. BUSINESS DOMAINS
    // ========================================================================

    // --- OTP ---
    OTP_INVALID(HttpStatus.BAD_REQUEST, "OTP_001", "Invalid OTP"),
    OTP_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "OTP_002", "OTP limit exceeded"),

    // --- Product ---
    SLUG_EXISTED(HttpStatus.CONFLICT, "SLUG_001", "Slug already exists"),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_001", "Product not found"),
    PRODUCT_SKU_EXISTED(HttpStatus.CONFLICT, "PRODUCT_003" , "Product SKU already exists"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_004", "Category not found"),
    CATEGORY_INACTIVE(HttpStatus.BAD_REQUEST, "PRODUCT_005", "Category inactive"),
    CATEGORY_NAME_EXISTS(HttpStatus.CONFLICT, "PRODUCT_006", "Category name exists"),
    CATEGORY_SLUG_EXISTS(HttpStatus.CONFLICT, "PRODUCT_007", "Category slug exists"),
    CATEGORY_HAS_CHILDREN(HttpStatus.CONFLICT, "PRODUCT_008", "Category has children"),
    CATEGORY_PARENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_009", "Category parent not found"),

    // --- Product Validation ---
    PRODUCT_NAME_CONTAINS_BAD_WORDS(HttpStatus.BAD_REQUEST, "PRODUCT_010", "Product name contains inappropriate words"),
    PRODUCT_DESCRIPTION_CONTAINS_BAD_WORDS(HttpStatus.BAD_REQUEST, "PRODUCT_011", "Product description contains inappropriate words"),
    PRODUCT_INVALID_NAME_FORMAT(HttpStatus.BAD_REQUEST, "PRODUCT_012", "Product name contains invalid characters"),
    PRODUCT_INVALID_DESCRIPTION_FORMAT(HttpStatus.BAD_REQUEST, "PRODUCT_013", "Product description contains invalid content"),
    PRODUCT_EMPTY_CONTENT(HttpStatus.BAD_REQUEST, "PRODUCT_014", "Product empty content fields is not allowed"),
    PRODUCT_INVALID_PRICE(HttpStatus.BAD_REQUEST, "PRODUCT_014", "Product price must be positive"),
    PRODUCT_ORIGINAL_PRICE_LESS_THAN_BASE(HttpStatus.BAD_REQUEST, "PRODUCT_015", "Original price cannot be less than base price"),

    // --- Product Assets ---
    PRODUCT_ASSET_NOT_FOUND(HttpStatus.NOT_FOUND, "ASSET_001", "Product asset not found"),
    PRODUCT_ASSET_INVALID_URL(HttpStatus.BAD_REQUEST, "ASSET_002", "Invalid asset URL"),
    PRODUCT_ASSET_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "ASSET_003", "Maximum 10 assets allowed per product"),
    PRODUCT_ASSET_DUPLICATE_URL(HttpStatus.CONFLICT, "ASSET_004", "Asset URL already exists for this product"),
    PRODUCT_MUST_HAVE_THUMBNAIL(HttpStatus.BAD_REQUEST, "ASSET_005", "Product must have at least one thumbnail"),

    // --- Product Categories ---
    PRODUCT_CATEGORY_ALREADY_EXISTS(HttpStatus.CONFLICT, "PROD_CAT_001", "Product already has this category"),
    PRODUCT_CATEGORY_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "PROD_CAT_002", "Maximum 5 categories allowed per product"),
    PRODUCT_MUST_HAVE_CATEGORY(HttpStatus.BAD_REQUEST, "PROD_CAT_003", "Product must have at least one category"),
    PRODUCT_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PROD_CAT_004", "Product category association not found"),

    // --- MATERIAL ----
    MATERIAL_NOT_FOUND(HttpStatus.NOT_FOUND, "MATERIAL_001", "Material not found"),

    // --- CURRENCY ---
    UNSUPPORTED_CURRENCY(HttpStatus.BAD_REQUEST, "CURRENCY_001", "Unsupported currency"),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "CURRENCY_002", "Invalid price"),
    INVALID_PRICE_2(HttpStatus.BAD_REQUEST, "CURRENCY_003", "Invalid price 2"),
    INVALID_PRICE_3(HttpStatus.BAD_REQUEST, "CURRENCY_004", "Invalid price 3"),

    // --- ROLE/PERMISSION ---
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROLE_001", "Role not found"),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "OUT_OF_STOCK" , "Out of stock" ),
    RESOURCE_CONFLICT(HttpStatus.CONFLICT, "RESOURCE_CONFLICT" , "Resource conflict" ),

    // --- CART ---
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_001", "Cart not found"),
    CART_ALREADY_EXISTS(HttpStatus.CONFLICT, "CART_002", "Cart already exists"),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_003", "Cart item not found"),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "CART_004", "Insufficient stock"),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "CART_005", "Invalid quantity"),
    CART_EMPTY(HttpStatus.BAD_REQUEST, "CART_006", "Cart empty"),

    // ---- PROMOTION -----
    // --- Basic ---
    PROMOTION_NOT_FOUND(HttpStatus.NOT_FOUND, "PROMOTION_001", "Promotion not found"),
    PROMOTION_INACTIVE(HttpStatus.BAD_REQUEST, "PROMOTION_002", "Promotion inactive"),

    // --- Time ---
    PROMOTION_EXPIRED(HttpStatus.BAD_REQUEST, "PROMOTION_003", "Promotion expired"),
    PROMOTION_NOT_STARTED(HttpStatus.BAD_REQUEST, "PROMOTION_004", "Promotion not started"),

    // --- Conditions ---
    PROMOTION_MIN_SPEND_REQUIRED(HttpStatus.BAD_REQUEST, "PROMOTION_005", "Promotion min spend required"),
    PROMOTION_CONDITION_NOT_MET(HttpStatus.BAD_REQUEST, "PROMOTION_006", "Promotion condition not met"),

    // --- Limits ---
    PROMOTION_USAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "PROMOTION_007", "Promotion usage limit exceeded"),
    PROMOTION_USER_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "PROMOTION_008", "Promotion user limit exceeded"),

    // --- Other ---
    INVALID_PROMOTION_CODE(HttpStatus.BAD_REQUEST, "PROMOTION_009", "Invalid promotion code"),
    // ----- ORDER --------
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_001", "Order not found"),
    ORDER_PAID(HttpStatus.BAD_REQUEST, "ORDER_002", "Order paid"),
    // ----- PAYMENT ------
    PAYMENT_METHOD_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "PAYMENT_METHOD_001" , "Payment method not supported" ),
    
    // ----- INVENTORY ------
    INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "INVENTORY_001", "Inventory not found"),
    INVENTORY_INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "INVENTORY_002", "Inventory insufficient stock"),
    INVENTORY_BELOW_RESERVED(HttpStatus.BAD_REQUEST, "INVENTORY_003", "Inventory below reserved"),
    INVENTORY_INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "INVENTORY_004", "Inventory invalid quantity"),
    INVENTORY_RESERVE_FAILED(HttpStatus.CONFLICT, "INVENTORY_005", "Inventory reserve failed"),
    INVENTORY_CONFIRM_FAILED(HttpStatus.CONFLICT, "INVENTORY_006", "Inventory confirm failed"),
    INVENTORY_RELEASE_FAILED(HttpStatus.CONFLICT, "INVENTORY_007", "Inventory release failed"),
    
    UNCATEGORIZED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,"PAYMENT_ERROR_002" , "Uncategorized exception");


    // ... (rest of simple constructor)
    
    private final HttpStatus httpStatus;
    private final String code;
    private final String message; // Renamed from messageKey

    public String format(Object... args) {
        return message;
    }

    public static String getMessage(String code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode.getMessage();
            }
        }
        return null;
    }

}