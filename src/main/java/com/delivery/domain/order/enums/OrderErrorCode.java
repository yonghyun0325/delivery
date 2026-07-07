package com.delivery.domain.order.enums;

public enum OrderErrorCode {

    // 400 Bad Request
    INVALID_ORDER_REQUEST(400, "잘못된 주문 요청입니다."),
    INVALID_ORDER_QUANTITY(400, "주문 수량은 1개 이상이어야 합니다."),
    INVALID_ORDER_DATE_RANGE(400, "조회 시작일은 종료일보다 늦을 수 없습니다."),

    INVALID_PAGE_SIZE(400, "페이지 크기는 10, 30, 50만 가능합니다."),

    MENU_STORE_MISMATCH(400, "해당 가게의 메뉴가 아닙니다."),
    ORDER_STORE_MISMATCH(400, "해당 가게의 주문이 아닙니다."),

    INVALID_ORDER_STATUS_TRANSITION(400, "변경할 수 없는 주문 상태입니다."),
    ORDER_CANCEL_TIME_EXPIRED(400, "주문 생성 후 5분이 지나 취소할 수 없습니다."),
    ORDER_CANCEL_NOT_ALLOWED(400, "현재 상태에서는 주문을 취소할 수 없습니다."),
    ORDER_COMPLETE_NOT_ALLOWED(400, "현재 상태에서는 주문을 완료할 수 없습니다."),
    ORDER_ALREADY_TERMINATED(400, "이미 종료된 주문입니다."),

    // 401 Unauthorized
    UNAUTHORIZED(401, "로그인이 필요합니다."),

    // 403 Forbidden
    FORBIDDEN_ORDER_ACCESS(403, "해당 주문에 접근할 권한이 없습니다."),
    FORBIDDEN_ORDER_STATUS_CHANGE(403, "주문 상태를 변경할 권한이 없습니다."),

    // 404 Not Found
    ORDER_NOT_FOUND(404, "주문을 찾을 수 없습니다."),
    STORE_NOT_FOUND(404, "가게를 찾을 수 없습니다."),
    MENU_NOT_FOUND(404, "메뉴를 찾을 수 없습니다.");

    private final int status;
    private final String message;

    OrderErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

}
