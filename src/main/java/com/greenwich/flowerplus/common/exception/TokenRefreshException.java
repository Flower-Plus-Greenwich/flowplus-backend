package com.greenwich.flowerplus.common.exception;

import com.greenwich.flowerplus.common.enums.ErrorCode;

public class TokenRefreshException extends AppException {
    public TokenRefreshException(String token, String message) {
        super(ErrorCode.TOKEN_INVALID, token, message);
    }
}
