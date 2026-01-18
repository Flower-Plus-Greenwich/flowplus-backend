package com.greenwich.flowerplus.common.exception;


import com.greenwich.flowerplus.common.enums.ErrorCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Getter
@Slf4j
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode, Object... args) {
        super(resolveMessage(errorCode, args));
        this.errorCode = errorCode != null ? errorCode : ErrorCode.INTERNAL_ERROR;
    }

    private static String resolveMessage(ErrorCode errorCode, Object... args) {
        if (errorCode == null) {
            return String.format("%s", args);
        }
        return errorCode.format(args);
    }

    public String getErrorCode() {
        return errorCode.getCode();
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}