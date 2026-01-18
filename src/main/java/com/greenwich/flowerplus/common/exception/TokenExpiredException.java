package com.greenwich.flowerplus.common.exception;

import com.greenwich.flowerplus.common.enums.ErrorCode;

public class TokenExpiredException extends AppException {
    public TokenExpiredException(Object... messageArgs) {
        super(ErrorCode.TOKEN_EXPIRED, messageArgs);
    }
}
