package com.delivery.domain.reviewreply.exception;

import com.delivery.global.exception.BusinessException;

public class ReviewReplyException extends BusinessException {

    public ReviewReplyException(ReviewReplyErrorCode errorCode) {
        super(errorCode);
    }
}
