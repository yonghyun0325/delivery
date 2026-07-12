package com.delivery.domain.menu.exception;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.menu.controller.MenuController;
import com.delivery.global.exception.ErrorCode;
import com.delivery.global.exception.GlobalErrorCode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// MenuController에서 발생하는 예외만 처리 - GlobalExceptionHandler(전역, 다른 도메인 필드명과 공유)를
// 건드리지 않고도 name/price 검증 실패에 정확한 메뉴 전용 에러코드를 응답할 수 있음.
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = MenuController.class)
public class MenuExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestApiResponse<?>> handleMenuValidation(
            MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();

        ErrorCode errorCode =
                switch (fieldError != null ? fieldError.getField() : "") {
                    case "name" -> MenuErrorCode.INVALID_MENU_NAME;
                    case "price" -> MenuErrorCode.INVALID_MENU_PRICE;
                    default -> GlobalErrorCode.BAD_REQUEST;
                };

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(
                        RestApiResponse.fail(
                                errorCode.getHttpStatus(),
                                errorCode.getMessage(),
                                errorCode.getName()));
    }
}
