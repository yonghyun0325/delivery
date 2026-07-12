package com.delivery.domain.menu.exception;

import com.delivery.global.exception.BusinessException;

public class MenuException extends BusinessException {

    public MenuException(MenuErrorCode errorCode) {
        super(errorCode);
    }
}
