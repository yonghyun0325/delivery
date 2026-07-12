package com.delivery.domain.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderItemCreateRequest(
        // 고객 주문 생성 요청 DTO 에 포함
        // 요청에 담긴 주문 메뉴 목록
        @NotNull(message = "메뉴 ID는 필수입니다.")
        UUID menuId,

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "주문 수량은 1개 이상이어야 합니다.")
        Integer quantity

        // 서버에서 관리해야 할 값
        /*
        menuName    → menuId로 메뉴 조회 후 스냅샷 저장
        menuPrice   → menuId로 메뉴 조회 후 스냅샷 저장
        */
) {
}