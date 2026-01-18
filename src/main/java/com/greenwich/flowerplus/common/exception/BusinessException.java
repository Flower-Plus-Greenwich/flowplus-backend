package com.greenwich.flowerplus.common.exception;

import com.greenwich.flowerplus.common.enums.ErrorCode;

public class BusinessException extends AppException {
    public BusinessException(Object... args) {
        super(ErrorCode.PRODUCT_NOT_FOUND, args);
    }

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}
