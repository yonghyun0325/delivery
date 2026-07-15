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
        @NotNull(message = "ORDER_STORE_ID_REQUIRED") UUID storeId,
        @NotBlank(message = "ORDER_DELIVERY_ADDRESS_REQUIRED") @Size(max = 255, message = "ORDER_DELIVERY_ADDRESS_TOO_LONG")
                String deliveryAddress,
        @NotEmpty(message = "ORDER_ITEMS_REQUIRED") @Valid List<OrderItemCreateRequest> items

        // 서버에서 관리해야 할 값
        /*
        status      → 서버에서 REQUESTED로 생성
        totalPrice  → 주문 상세 subtotal 합계로 계산
        createdAt   → 서버에서 생성 시점 저장
        */
        ) {}
