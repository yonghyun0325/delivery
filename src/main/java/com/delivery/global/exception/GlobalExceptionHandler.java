package com.delivery.global.exception;

import com.delivery.common.RestApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
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
        log.warn(
                "ErrorCode : {}, ErrorMessage : {}",
                errorCode.getName(),
                errorCode.getMessage(),
                e);

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

        log.warn(
                "ErrorCode : {}, ErrorMessage : {}",
                errorCode.getName(),
                errorCode.getMessage(),
                e);

        return buildResponseEntity(errorCode);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RestApiResponse<?>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        ErrorCode errorCode = GlobalErrorCode.INVALID_PARAMETER_TYPE;
        log.warn(
                "ErrorCode : {}, ErrorMessage : {}",
                errorCode.getName(),
                errorCode.getMessage(),
                e);

        return buildResponseEntity(errorCode);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<RestApiResponse<?>> handleAuthorizationDeniedException(
            AuthorizationDeniedException e, HttpServletRequest request) {
        ErrorCode errorCode = GlobalErrorCode.FORBIDDEN;

        log.warn(
                "ErrorCode : {}, ErrorMessage : {}",
                errorCode.getName(),
                errorCode.getMessage(),
                e);

        return buildResponseEntity(errorCode);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestApiResponse<?>> handleException(Exception e) {
        ErrorCode errorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR;
        log.error(
                "ErrorCode : {}, ErrorMessage : {}",
                errorCode.getName(),
                errorCode.getMessage(),
                e);

        return buildResponseEntity(errorCode);
    }

    // TODO : HttpMessageNotReadableException 에러 처리
    // TODO : InsufficientAuthenticationException 에러 처리 만료된 토큰

    private ResponseEntity<RestApiResponse<?>> buildResponseEntity(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(
                        RestApiResponse.fail(
                                errorCode.getHttpStatus(),
                                errorCode.getMessage(),
                                errorCode.getName()));
    }
}
