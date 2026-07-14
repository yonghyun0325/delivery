package com.delivery.domain.order.dto.response;

import com.delivery.domain.order.entity.Order;
import java.util.List;
import org.springframework.data.domain.Page;

public record OrderListResponse(
        // 고객이나 가게(본인) 주문 내역 확인할 때 활용
        // 주문 여러 건, 페이징 정보
        List<OrderSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static OrderListResponse from(Page<Order> orders) {
        return new OrderListResponse(
                orders.getContent().stream().map(OrderSummaryResponse::from).toList(),
                orders.getNumber(),
                orders.getSize(),
                orders.getTotalElements(),
                orders.getTotalPages());
    }
}
