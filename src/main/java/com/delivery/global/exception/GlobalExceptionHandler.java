package com.delivery.global.exception;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.auth.dto.LoginRequestDto;
import com.delivery.domain.auth.exception.AuthErrorCode;
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
    protected ResponseEntity<RestApiResponse<?>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        HttpStatus httpStatus = errorCode.getHttpStatus();
        String message = errorCode.getMessage();
        String error = errorCode.getName();

        log.warn("{} : {}", error, message, e);

        return ResponseEntity.status(httpStatus)
                .body(RestApiResponse.fail(httpStatus, message, error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestApiResponse<?>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        var target = e.getBindingResult().getTarget();

        ErrorCode errorCode = GlobalErrorCode.BAD_REQUEST;

        // 로그인 Dto
        if (target instanceof LoginRequestDto) {
            errorCode = AuthErrorCode.INVALID_LOGIN;
        } else if (fieldError != null) {
            errorCode =
                    switch (fieldError.getField()) {
                            // 로그인 관련 에러
                        case "username" -> UserErrorCode.INVALID_USERNAME;
                        case "password" -> UserErrorCode.INVALID_PASSWORD;
                        case "nickName" -> UserErrorCode.INVALID_NICKNAME;
                        case "phoneNumber" -> UserErrorCode.INVALID_PHONE_NUMBER;

                            // 기본 값
                        default -> GlobalErrorCode.BAD_REQUEST;
                    };
        }

        log.warn("{} : {}", errorCode.getName(), errorCode.getMessage(), e);

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(
                        RestApiResponse.fail(
                                errorCode.getHttpStatus(),
                                errorCode.getMessage(),
                                errorCode.getName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RestApiResponse<?>> handleMethodArgumentTypeMismatchException(
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
