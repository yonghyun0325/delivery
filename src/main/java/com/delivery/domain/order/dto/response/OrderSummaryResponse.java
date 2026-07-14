package com.delivery.domain.order.dto.response;

import com.delivery.domain.order.entity.Order;
import com.delivery.domain.order.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderSummaryResponse(
        // 고객이나 가게(본인) 주문 내역 확인할 때 활용
        // 주문 1건 요약 정보
        UUID orderId,
        Long userId,
        UUID storeId,
        OrderStatus status,
        Integer totalPrice,
        LocalDateTime createdAt) {

    public static OrderSummaryResponse from(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getUserId(),
                order.getStoreId(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getCreatedAt());
    }
}
