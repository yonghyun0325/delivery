package com.delivery.domain.order.dto.response;

import com.delivery.domain.order.entity.Order;
import com.delivery.domain.order.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(
        // 주문 단건 조회 DTO
        UUID orderId,
        Long userId,
        UUID storeId,
        OrderStatus status,
        String deliveryAddress,
        Integer totalPrice,
        LocalDateTime cancelledAt,
        LocalDateTime completedAt,
        List<OrderItemResponse> items,
        LocalDateTime createdAt) {
    public static OrderDetailResponse from(Order order) {
        return new OrderDetailResponse(
                order.getId(),
                order.getUserId(),
                order.getStoreId(),
                order.getStatus(),
                order.getDeliveryAddress(),
                order.getTotalPrice(),
                order.getCancelledAt(),
                order.getCompletedAt(),
                order.getOrderItems().stream().map(OrderItemResponse::from).toList(),
                order.getCreatedAt());
    }
}
