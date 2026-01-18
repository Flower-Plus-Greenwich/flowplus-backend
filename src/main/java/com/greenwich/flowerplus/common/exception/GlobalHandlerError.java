package com.greenwich.flowerplus.common.exception;



import com.fasterxml.jackson.core.exc.InputCoercionException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import com.greenwich.flowerplus.common.ApiResult;
import com.greenwich.flowerplus.common.enums.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class GlobalHandlerError {

    // --- 0. BUSINESS LOGIC ERRORS (CORE) ---
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResult<?>> handleAppException(AppException e) {
        String traceId = getTraceId();
        log.warn("Business error [{}]: {} - Code: {}", traceId, e.getMessage(), e.getErrorCode());

        return ResponseEntity
                .status(e.getHttpStatus())
                .body(ApiResult.error(
                        e.getErrorCode(),
                        e.getMessage(),
                        traceId
                ));
    }

    // --- 1. VALIDATION EXCEPTIONS (400) ---
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class,
    })
    public ResponseEntity<ApiResult<?>> handleValidationException(Exception e) {
        String traceId = getTraceId();
        log.warn("Validation error [{}]: {}", traceId, e.getMessage());

        Object errorDetails = null;
        String message = "Validation failed";

        // 1.1 Error from @Valid on DTO
        if (e instanceof MethodArgumentNotValidException ex) {
            BindingResult result = ex.getBindingResult();
            Map<String, String> errors = new HashMap<>();
            for (FieldError fieldError : result.getFieldErrors()) {
                // Use default message directly as we replaced keys with messages in DTOs
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
            errorDetails = errors;
            message = "Invalid input data";
        }
        // 1.2 Error from @Validated on Controller
        else if (e instanceof ConstraintViolationException ex) {
            Map<String, String> errors = ex.getConstraintViolations().stream()
                    .collect(Collectors.toMap(
                            violation -> violation.getPropertyPath().toString(),
                            violation -> violation.getMessage(), // Message is now hardcoded text
                            (msg1, msg2) -> msg1
                    ));
            errorDetails = errors;
            message = "Invalid parameters";
        }
        // 1.3 Missing parameter
        else if (e instanceof MissingServletRequestParameterException ex) {
            message = "Missing required parameter: " + ex.getParameterName();
        }
        // 1.4 Malformed JSON / Jackson errors
        else if (e instanceof HttpMessageNotReadableException ex) {
            Throwable root = ex.getMostSpecificCause();
            log.debug(root.getMessage(), root);
            
            if (root instanceof InputCoercionException ice) {
                String field = extractFieldFromPath(ice);
                message = "Numeric value out of range for field: " + field;
            }
            else if (root instanceof InvalidFormatException ife) {
                String field = ife.getPath().stream()
                        .map(JsonMappingException.Reference::getFieldName)
                        .collect(Collectors.joining("."));
                
                Class<?> type = ife.getTargetType();
                if (Number.class.isAssignableFrom(type) || type.isPrimitive()) {
                    message = "Number out of range for field: " + field;
                } else {
                    message = "Invalid format for field: " + field;
                }
            }
            else {
                message = "Malformed JSON request";
            }
        }
        else if (e instanceof IllegalArgumentException ex) {
            message = ex.getMessage();
        }
        else if (e instanceof InvalidFormatException ex) {
            String fieldPath = ex.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .collect(Collectors.joining("."));
            message = "Invalid format for field: " + fieldPath;
        }

        return ResponseEntity.status(BAD_REQUEST)
                .body(ApiResult.error(
                        ErrorCode.INVALID_REQUEST.getCode(),
                        message,
                        traceId,
                        errorDetails
                ));
    }

    private String extractFieldFromPath(Throwable ex) {
        String msg = ex.getMessage();
        if (msg == null) return null;

        Pattern p = Pattern.compile("\\[\"(.*?)\"]");
        Matcher m = p.matcher(msg);
        if (m.find()) return m.group(1);

        return null;
    }

    // --- 2. AUTHENTICATION & AUTHORIZATION (401, 403) ---

    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class,
            TokenExpiredException.class,
            TokenNotFoundException.class
    })
    public ResponseEntity<ApiResult<?>> handleAuthException(Exception e) {
        String traceId = getTraceId();
        log.warn("Auth error [{}]: {}", traceId, e.getMessage());

        return ResponseEntity.status(UNAUTHORIZED)
                .body(ApiResult.error(
                        ErrorCode.UNAUTHENTICATED.getCode(),
                        e.getMessage(), // Or generic "Authentication failed" if security needs hiding
                        traceId
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResult<?>> handleAccessDeniedException(AccessDeniedException e) {
        String traceId = getTraceId();
        log.warn("Access denied [{}]: {}", traceId, e.getMessage());

        return ResponseEntity.status(FORBIDDEN)
                .body(ApiResult.error(
                        ErrorCode.ACCESS_DENIED.getCode(),
                        ErrorCode.ACCESS_DENIED.getMessage(),
                        traceId
                ));
    }

    // --- 3. NOT FOUND (404) ---
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ApiResult<?>> handleNotFoundException(HttpServletRequest request, Exception e) {
        String traceId = getTraceId();
        String wrongUrl = request.getRequestURI();
        return ResponseEntity.status(NOT_FOUND)
                .body(ApiResult.error(
                        ErrorCode.RESOURCE_NOT_FOUND.getCode(),
                        "Resource not found: " + wrongUrl,
                        traceId
                ));
    }

    // --- 4. TOKEN SPECIFIC ---
    @ExceptionHandler(TokenErrorException.class)
    public ResponseEntity<ApiResult<?>> handleTokenErrorException(TokenErrorException e) {
        String traceId = getTraceId();
        log.warn("Token error [{}]: {}", traceId, e.getMessage());

        return ResponseEntity.status(e.getHttpStatus())
                .body(ApiResult.error(
                        e.getErrorCode(),
                        e.getMessage(),
                        traceId
                ));
    }

    // --- 5. SYSTEM ERRORS (500) ---
    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ApiResult<?>> handleMessagingException(MessagingException e) {
        String traceId = getTraceId();
        log.error("Email error [{}]: ", traceId, e);

        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(ApiResult.error(
                        ErrorCode.INTERNAL_ERROR.getCode(),
                        "Email service error",
                        traceId
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<?>> handleGlobalException(Exception e) {
        String traceId = getTraceId();
        log.error("Internal Server Error [{}]: ", traceId, e);

        String friendlyMessage = "An unexpected error occurred";

        if (e instanceof HttpRequestMethodNotSupportedException) {
            return ResponseEntity.status(METHOD_NOT_ALLOWED)
                    .body(ApiResult.error(ErrorCode.METHOD_NOT_ALLOWED.getCode(), e.getMessage(), traceId));
        }

        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(ApiResult.error(
                        ErrorCode.INTERNAL_ERROR.getCode(),
                        friendlyMessage,
                        traceId
                ));
    }

    @ExceptionHandler(OtpCodeException.class)
    public ResponseEntity<ApiResult<?>> handleOtpCodeException(OtpCodeException e) {
        String traceId = getTraceId();
        log.warn("OTP Code error [{}]: {}", traceId, e.getMessage());

        return ResponseEntity.status(BAD_REQUEST)
                .body(ApiResult.error(
                        e.getErrorCode(),
                        e.getMessage(),
                        traceId
                ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResult<?>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        String traceId = getTraceId();
        log.warn("File upload error [{}]: {}, maximum : {}", traceId, e.getMessage(), e.getMaxUploadSize());

        String message = e.getMessage();
        if (message.contains("Maximum upload size exceeded")) {
            message = "Maximum upload size exceeded. Limit is: " + e.getMaxUploadSize();
        }

        return ResponseEntity.status(BAD_REQUEST)
                .body(ApiResult.error(
                        ErrorCode.FILE_UPLOAD_FAILED.getCode(),
                        message,
                        traceId
                ));
    }


    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResult<?>> handleConcurrencyError(ObjectOptimisticLockingFailureException ex) {
        String traceId = getTraceId();
        log.warn("Object optimistic locking error [{}]: {}", traceId, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResult.error(
                        ErrorCode.RESOURCE_CONFLICT.getCode(),
                        "Resource was updated by another user. Please refresh and try again.",
                        getTraceId()
                        ));
    }

    // --- Helper Methods ---
    private String getTraceId() {
        return UUID.randomUUID().toString();
    }
}