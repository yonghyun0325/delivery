package com.delivery.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 에러코드 인터페이스 Enum으로 상속 받아서 사용 ex) UserErrorCode, OrderErrorCode, StoreErrorCode BusinessException
 * 공통으로 사용
 */
public interface ErrorCode {
    HttpStatus getHttpStatus();

    String getMessage();

    String getName();
}
