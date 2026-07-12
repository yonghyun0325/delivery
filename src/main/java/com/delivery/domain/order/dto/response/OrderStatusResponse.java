package com.delivery.domain.order.dto.response;

import com.delivery.domain.order.entity.Order;
import com.delivery.domain.order.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderStatusResponse(
        // 주문 상태 변경 응답 DTO
        UUID orderId,
        OrderStatus status,
        LocalDateTime cancelledAt,
        LocalDateTime completedAt

) {
    public static OrderStatusResponse from(Order order) {
        return new OrderStatusResponse(
                order.getId(),
                order.getStatus(),
                order.getCancelledAt(),
                order.getCompletedAt()
        );
    }
}
