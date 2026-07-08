package com.delivery.domain.order.repository;

import com.delivery.domain.order.entity.Order;
import com.delivery.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {


    // 주문 단건 조회
    /*Order 조회 시 연관된 OrderItem 목록도 함께 조회
    Soft Delete 처리된 주문은 조회 대상에서 제외*/
    @EntityGraph(attributePaths = "orderItems")
    Optional<Order> findByIdAndDeletedAtIsNull(UUID orderId);

}