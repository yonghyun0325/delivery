package com.delivery.domain.review.exception;

import com.delivery.global.exception.BusinessException;

public class ReviewException extends BusinessException {

    public ReviewException(ReviewErrorCode errorCode) {
        super(errorCode);
    }
}
