package com.delivery.domain.order.exception;

import com.delivery.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum OrderErrorCode implements ErrorCode {

    // 400 Bad Request
    // 주문 생성 검증
    INVALID_ORDER_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 주문 요청입니다."),
    INVALID_ORDER_QUANTITY(HttpStatus.BAD_REQUEST, "주문 수량은 1개 이상이어야 합니다."),
    ORDER_STORE_MISMATCH(HttpStatus.BAD_REQUEST, "해당 가게의 주문이 아닙니다."),
    STORE_NOT_OPEN(HttpStatus.BAD_REQUEST, "현재 영업 중인 가게가 아닙니다."),
    MINIMUM_ORDER_AMOUNT_NOT_MET(HttpStatus.BAD_REQUEST, "최소 주문 금액을 충족하지 못했습니다."),

    // 조회 조건 검증
    INVALID_ORDER_DATE_RANGE(HttpStatus.BAD_REQUEST, "조회 시작일은 종료일보다 늦을 수 없습니다."),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "페이지 크기는 10, 30, 50만 가능합니다."),

    // 상태 전이 검증 및 주문 취소 완료 정책
    INVALID_ORDER_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "변경할 수 없는 주문 상태입니다."),
    ORDER_CANCEL_TIME_EXPIRED(HttpStatus.BAD_REQUEST, "주문 생성 후 5분이 지나 취소할 수 없습니다."),
    ORDER_CANCEL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "현재 상태에서는 주문을 취소할 수 없습니다."),
    ORDER_COMPLETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "현재 상태에서는 주문을 완료할 수 없습니다."),
    ORDER_ALREADY_TERMINATED(HttpStatus.BAD_REQUEST, "이미 종료된 주문입니다."),

    // 401 Unauthorized
    // 인증 및 인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),

    // 403 Forbidden
    // 권한
    FORBIDDEN_ORDER_ACCESS(HttpStatus.FORBIDDEN, "해당 주문에 접근할 권한이 없습니다."),
    FORBIDDEN_ORDER_STATUS_CHANGE(HttpStatus.FORBIDDEN, "주문 상태를 변경할 권한이 없습니다."),
    FORBIDDEN_STORE_ACCESS(HttpStatus.FORBIDDEN, "해당 가게에 접근할 권한이 없습니다."),

    // 404 Not Found
    // 리소스 조회 실패
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    OrderErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String getName() {
        return this.name();
    }
}
