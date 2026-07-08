package com.delivery.domain.user.exception;

import com.delivery.global.exception.BusinessException;
import com.delivery.global.exception.ErrorCode;

public class UserException extends BusinessException {
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }
}
