package com.greenwich.flowerplus.common.exception;

import com.greenwich.flowerplus.common.enums.ErrorCode;

public class TokenNotFoundException extends AppException {
    public TokenNotFoundException(Object... messageArgs) {
        super(ErrorCode.TOKEN_INVALID, messageArgs);
    }
}
