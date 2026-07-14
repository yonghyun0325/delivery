package com.delivery.domain.payment.exception;

import com.delivery.global.exception.BusinessException;

public class PaymentException extends BusinessException {

    public PaymentException(PaymentErrorCode errorCode) {
        super(errorCode);
    }
}
