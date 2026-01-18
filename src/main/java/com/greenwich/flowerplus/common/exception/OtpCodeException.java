package com.greenwich.flowerplus.common.exception;

import com.greenwich.flowerplus.common.enums.ErrorCode;

public class OtpCodeException extends AppException {
    public OtpCodeException() {
        super(ErrorCode.OTP_INVALID);
    }
}
