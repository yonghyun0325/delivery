package com.delivery.domain.order.dto.response;

import com.delivery.domain.order.entity.Order;
import com.delivery.domain.order.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCreateResponse(
        // 고객 주문 생성 응답 DTO
        UUID orderId,
        UUID storeId,
        OrderStatus status,
        String deliveryAddress,
        Integer totalPrice,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
    public static OrderCreateResponse from(Order order) {
        return new OrderCreateResponse(
                order.getId(),
                order.getStoreId(),
                order.getStatus(),
                order.getDeliveryAddress(),
                order.getTotalPrice(),
                order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                order.getCreatedAt()
        );
    }
}