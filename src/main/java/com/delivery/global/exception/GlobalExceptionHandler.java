package com.delivery.global.exception;

import com.delivery.common.RestApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<RestApiResponse<?>> handBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        HttpStatus httpStatus = errorCode.getHttpStatus();
        String message = errorCode.getMessage();
        String error = errorCode.getName();

        log.error(error + " : " + message);

        return ResponseEntity.status(httpStatus)
                .body(RestApiResponse.fail(httpStatus, message, error));
    }
}
