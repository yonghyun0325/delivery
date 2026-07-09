package com.delivery.domain.ai.exception;

import com.delivery.global.exception.BusinessException;

public class AiException extends BusinessException {

    public AiException(AiErrorCode errorCode) {
        super(errorCode);
    }
}
