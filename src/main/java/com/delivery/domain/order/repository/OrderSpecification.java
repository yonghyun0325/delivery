package com.delivery.domain.order.repository;

import com.delivery.domain.order.entity.Order;
import com.delivery.domain.order.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecification {
    // 실제 검색 조건 생성

    // PostgreSQL에서 null 파라미터 타입을 못 정해서 터지는 문제를 피하기 위해
    // status, startDate, endDate가 없으면 조건 자체를 만들지 않음

    // 유틸 클래스처럼 사용할 것이므로 객체 생성을 막음
    // OrderSpecification.userIdEquals(...) 형태로 static 메서드만 사용
    private OrderSpecification() {}

    // 현재 로그인한 고객의 주문만 조회하기 위한 조건
    // SQL 관점: where user_id = ?
    public static Specification<Order> userIdEquals(Long userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("userId"), userId);
    }

    // Soft Delete되지 않은 주문만 조회하기 위한 조건
    // SQL 관점: where deleted_at is null
    public static Specification<Order> deletedAtIsNull() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt"));
    }

    // 주문 상태로 필터링하기 위한 조건
    // status가 null이면 조건을 추가하지 않음
    // 예: status=REQUESTED인 경우에만 where status = 'REQUESTED' 조건 추가
    public static Specification<Order> statusEquals(OrderStatus status) {
        if (status == null) {
            return null;
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

    // 조회 시작일 조건
    // startDateTime이 null이면 조건을 추가하지 않음
    // SQL 관점: where created_at >= ?
    public static Specification<Order> createdAtGoe(LocalDateTime startDateTime) {
        if (startDateTime == null) {
            return null;
        }

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDateTime);
    }

    // 조회 종료일 조건
    // endDateTime이 null이면 조건을 추가하지 않음
    // SQL 관점: where created_at <= ?
    public static Specification<Order> createdAtLoe(LocalDateTime endDateTime) {
        if (endDateTime == null) {
            return null;
        }

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDateTime);
    }

    // 가게 주문 내역 조회 조건
    // PathVariable로 받은 storeId와 일치하는 주문만 조회
    public static Specification<Order> storeIdEquals(UUID storeId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("storeId"), storeId);
    }
}
