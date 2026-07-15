package com.delivery.domain.cart.exception;

import com.delivery.global.exception.BusinessException;

public class CartException extends BusinessException {

    public CartException(CartErrorCode errorCode) {
        super(errorCode);
    }
}
