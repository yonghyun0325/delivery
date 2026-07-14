package com.delivery.domain.order.dto.response;

import com.delivery.domain.order.entity.OrderItem;
import java.util.UUID;

public record OrderItemResponse(
        // 고객 주문 생성 응답 DTO 에 포함
        // 응답에 담긴 주문 메뉴 목록
        UUID orderItemId,
        UUID menuId,
        String menuName,
        Integer menuPrice,
        Integer quantity,
        Integer subtotalPrice) {
    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getMenuId(),
                orderItem.getMenuName(),
                orderItem.getMenuPrice(),
                orderItem.getQuantity(),
                orderItem.getSubtotalPrice());
    }
}
