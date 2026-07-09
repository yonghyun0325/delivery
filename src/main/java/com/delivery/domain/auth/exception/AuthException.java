package com.delivery.domain.auth.exception;

import com.delivery.global.exception.BusinessException;
import com.delivery.global.exception.ErrorCode;

public class AuthException extends BusinessException {
    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }
}
