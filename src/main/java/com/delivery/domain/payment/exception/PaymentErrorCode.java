package com.delivery.domain.payment.exception;

import com.delivery.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제를 찾을 수 없습니다."),
    PAYMENT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 결제가 생성된 주문입니다."),
    PAYMENT_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "이미 취소된 결제입니다."),
    PAYMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "결제에 접근할 권한이 없습니다."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "결제 금액은 0보다 커야 합니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST, "잘못된 결제 상태입니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getName() {
        return name();
    }
}
