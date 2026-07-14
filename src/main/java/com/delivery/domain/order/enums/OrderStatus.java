package com.delivery.domain.order.enums;

public enum OrderStatus { // 주문 상태 변경
    REQUESTED, // 고객 주문 요청
    ACCEPTED, // 가게 주문 수락
    COOKING, // 조리 중
    DELIVERING, // 배달 중
    DELIVERED, // 배달 완료
    COMPLETED, // 주문 최종 완료
    REJECTED, // 가게 주문 거절
    CUSTOMER_CANCELLED // 고객 주문 취소
}
