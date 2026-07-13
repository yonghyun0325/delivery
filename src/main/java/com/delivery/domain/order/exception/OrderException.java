package com.delivery.domain.order.exception;

import com.delivery.global.exception.BusinessException;

public class OrderException extends BusinessException {

    public OrderException(OrderErrorCode errorCode) {
        super(errorCode);
    }
}
