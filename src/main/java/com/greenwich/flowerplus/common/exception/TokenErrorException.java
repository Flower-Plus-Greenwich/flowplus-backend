package com.greenwich.flowerplus.common.exception;

import com.greenwich.flowerplus.common.enums.ErrorCode;

public class TokenErrorException extends AppException {
    public TokenErrorException(String detail) {
        super(ErrorCode.INTERNAL_ERROR, detail);
    }
}
