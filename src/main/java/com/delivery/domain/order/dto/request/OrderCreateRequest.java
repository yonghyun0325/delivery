package com.delivery.domain.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record OrderCreateRequest(
        // 고객 주문 생성 요청 DTO
        @NotNull(message = "가게 ID는 필수입니다.")
        UUID storeId,

        @NotBlank(message = "배달 주소는 필수입니다.")
        @Size(max = 255, message = "배달 주소는 255자 이하여야 합니다.")
        String deliveryAddress,

        @NotEmpty(message = "주문 상품은 1개 이상이어야 합니다.")
        @Valid
        List<OrderItemCreateRequest> items

        // 서버에서 관리해야 할 값
        /*
        status      → 서버에서 REQUESTED로 생성
        totalPrice  → 주문 상세 subtotal 합계로 계산
        createdAt   → 서버에서 생성 시점 저장
        */
) {
}