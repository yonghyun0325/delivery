package com.delivery.domain.order.repository;

import com.delivery.domain.order.entity.Order;
import com.delivery.domain.order.entity.OrderItem;
import com.delivery.domain.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {
                                                                        // 동적 검색 조건 + 페이징 조회 제공
    // 주문 단건 조회
    /*Order 조회 시 연관된 OrderItem 목록도 함께 조회
    Soft Delete 처리된 주문은 조회 대상에서 제외*/
    @EntityGraph(attributePaths = "orderItems")
    Optional<Order> findByIdAndDeletedAtIsNull(UUID orderId);




}