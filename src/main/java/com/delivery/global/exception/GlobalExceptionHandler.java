package com.delivery.global.exception;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.user.exception.UserErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<RestApiResponse<?>> handBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        HttpStatus httpStatus = errorCode.getHttpStatus();
        String message = errorCode.getMessage();
        String error = errorCode.getName();

        log.warn("{} : {}", error, message, e);

        return ResponseEntity.status(httpStatus)
                .body(RestApiResponse.fail(httpStatus, message, error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestApiResponse<?>> handMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        ErrorCode errorCode = GlobalErrorCode.BAD_REQUEST;

        if (fieldError != null) {
            errorCode =
                    switch (fieldError.getField()) {
                        case "username" -> UserErrorCode.INVALID_USERNAME;
                        case "password" -> UserErrorCode.INVALID_PASSWORD;
                        case "nickName" -> UserErrorCode.INVALID_NICKNAME;
                        case "phoneNumber" -> UserErrorCode.INVALID_PHONE_NUMBER;
                        default -> GlobalErrorCode.BAD_REQUEST;
                    };
        }
        String errorMessage =
                (fieldError != null) ? fieldError.getDefaultMessage() : errorCode.getMessage();
        if (errorMessage != null
                && (errorMessage.contains("must ")
                        || errorMessage.contains("match")
                        || errorMessage.contains("size"))) {
            errorMessage = errorCode.getMessage();
        }

        log.warn("{} : {}", errorCode.getName(), errorMessage, e);

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(
                        RestApiResponse.fail(
                                errorCode.getHttpStatus(), errorMessage, errorCode.getName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RestApiResponse<?>> handMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        ErrorCode errorCode = GlobalErrorCode.INVALID_PARAMETER_TYPE;

        log.warn("{} : {}", errorCode.getName(), errorCode.getMessage(), e);

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(
                        RestApiResponse.fail(
                                errorCode.getHttpStatus(),
                                errorCode.getMessage(),
                                errorCode.getName()));
    }
}
