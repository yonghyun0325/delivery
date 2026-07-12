package com.delivery.global.exception;

import com.delivery.common.RestApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ErrorCodeRegistry errorCodeRegistry;

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<RestApiResponse<?>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("{} : {}", errorCode.getName(), errorCode.getMessage(), e);

        return buildResponseEntity(errorCode);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestApiResponse<?>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String code = (fieldError != null) ? fieldError.getDefaultMessage() : null;
        ErrorCode errorCode = errorCodeRegistry.getByName(code);

        if (errorCode == null) {
            errorCode = GlobalErrorCode.INVALID_PARAMETER_TYPE;
        }

        log.warn("{} : {}", errorCode.getName(), errorCode.getMessage(), e);

        return buildResponseEntity(errorCode);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RestApiResponse<?>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        ErrorCode errorCode = GlobalErrorCode.INVALID_PARAMETER_TYPE;
        log.warn("{} : {}", errorCode.getName(), errorCode.getMessage(), e);

        return buildResponseEntity(errorCode);
    }

    private ResponseEntity<RestApiResponse<?>> buildResponseEntity(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(
                        RestApiResponse.fail(
                                errorCode.getHttpStatus(),
                                errorCode.getMessage(),
                                errorCode.getName()));
    }
}
