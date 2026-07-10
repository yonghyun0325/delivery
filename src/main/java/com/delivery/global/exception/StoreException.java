package com.delivery.global.exception;

public class StoreException extends BusinessException {

    public StoreException(StoreErrorCode errorCode) {
        super(errorCode);
    }
}
