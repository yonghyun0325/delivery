package com.delivery.domain.order.service;

import com.delivery.domain.order.dto.request.OrderCreateRequest;
import com.delivery.domain.order.dto.request.OrderItemCreateRequest;
import com.delivery.domain.order.dto.response.OrderCreateResponse;
import com.delivery.domain.order.dto.response.OrderDetailResponse;
import com.delivery.domain.order.dto.response.OrderListResponse;
import com.delivery.domain.order.entity.Order;
import com.delivery.domain.order.entity.OrderItem;
import com.delivery.domain.order.enums.OrderErrorCode;
import com.delivery.domain.order.enums.OrderStatus;
import com.delivery.domain.order.repository.OrderRepository;
import com.delivery.global.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static com.delivery.domain.order.repository.OrderSpecification.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    // 고객 주문 생성
    @Transactional
    public OrderCreateResponse createOrder(
            OrderCreateRequest request,
            Long currentUserId
    ){
        // 요청한 가게가 실제 존재하는지 확인
        // 아직 가게(store) 도메인 구조를 몰라 메서드만 분리해둠
        validateStoreExists(request.storeId());

        // TODO: BaseEntity / JPA Auditing 정책 확정 후 제거
        // 현재는 p_order.created_by 컬럼 저장을 위해 로그인 사용자 ID를 임시 사용
//        String temporaryCreatedBy = String.valueOf(currentUserId);

        // 주문 엔티티 생성
        /* userId는 요청값이 아니라 JWT에서 가져온 로그인 사용자 ID를 사용
         status는 생성 시 기본값 REQUESTED로 설정됨*/
        Order order = new Order(
                currentUserId,
                request.storeId(),
                request.deliveryAddress()
        );


        // 요청에 담긴 주문 메뉴 목록을 하나씩 처리
        for(OrderItemCreateRequest itemRequest : request.items()){

            // 수량 검증 (수량은 1개 이상)
            validateOrderQuantity(itemRequest.quantity());

            // menuId로 메뉴 정보를 조회하고,
            // 해당 메뉴가 요청한 storeId에 속한 메뉴인지 확인
            // 아직 메뉴 도메인 구조를 모르기 때문에 MenuSnapshot으로 필요한 값만 임시 정의
            MenuSnapshot menuSnapshot = getMenuSnapshot(
                    request.storeId(),
                    itemRequest.menuId()
            );

            // 주문 상세 엔티티 생성
            // menuName, menuPrice는 주문 당시 값으로 저장하는 스냅샷 데이터
            OrderItem orderItem = new OrderItem(
                    menuSnapshot.menuId(),
                    menuSnapshot.menuName(),
                    menuSnapshot.menuPrice(),
                    itemRequest.quantity()
            );

            // 주문에 주문 상세 추가
            /* addOrderItem 내부에서 Order와 OrderItem의 연관관계를 연결하고,
             totalPrice에 subtotalPrice를 누적함*/
            order.addOrderItem(orderItem);

        }

        // 주문 저장
        // Order에 Cascade 설정이 있어 OrderItem도 함께 저장됨
        Order savedOrder = orderRepository.save(order);

        // 저장된 주문 엔티티를 응답 DTO로 변환
        return OrderCreateResponse.from(savedOrder);
    }


    // 주문 단건 조회
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(
            UUID orderId,
            Long currentUserId
    ) {
        // 삭제되지 않은 주문 주회
        // 주문 단건 응답에 메뉴 상세 목록이 포함되므로 orderItems도 함께 조회
        Order order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        /*
         * TODO: Spring Security/JWT 및 권한(Role) 적용 후 접근 권한 검증 로직 확장 필요
         *
         * 주문 단건 조회 접근 정책:
         * 1. CUSTOMER
         *    - 본인이 생성한 주문만 조회 가능
         *    - order.userId == currentUserId 인 경우만 허용
         *
         * 2. OWNER
         *    - 본인 가게의 주문만 조회 가능
         *    - order.storeId가 로그인한 OWNER의 storeId와 일치하는 경우만 허용
         *
         * 3. MANAGER
         *    - 서비스 담당자 권한
         *    - 정책에 따라 전체 주문 또는 담당 범위 주문 조회 가능
         *
         * 4. MASTER
         *    - 최종 관리자 권한
         *    - 전체 주문 조회 가능
         */

        // 현재는 인증/인가 구조가 확정되지 않았으므로, CUSTOMER 기준으로 currentUserId와 order.userId만 임시 검증
        validateOrderAccessForCustomer(order, currentUserId);

        // 조회된 주문 엔티티 상세 응답 DTO 변환
        return OrderDetailResponse.from(order);

    }

    // 고객 본인 주문 내역 조회
    @Transactional(readOnly = true)
    public OrderListResponse getMyOrders(
            Long currentUserId,
            LocalDate startDate,
            LocalDate endDate,
            OrderStatus status,
            int page,
            int size,
            String sort
    ) {
        // 날짜 범위 검증
        // startDate가 endDate보다 늦으면 잘못된 조회 조건으로 판단
        validateDateRange(startDate, endDate);

        // LocalDate를 LocalDateTime으로 변환
        // startDate는 해당 날짜의 00:00:00부터 조회
        LocalDateTime startDateTime = startDate != null
                ? startDate.atStartOfDay()
                : null;

        // endDate는 해당 날짜의 마지막 시간까지 조회
        LocalDateTime endDateTime = endDate != null
                ? endDate.atTime(LocalTime.MAX)
                : null;

        // 페이지 크기 보정
        // 과제 조건에 따라 10, 30, 50만 허용하고 그 외 값은 10으로 고정
        int normalizedSize = normalizePageSize(size);

        // Pageable 생성
        // page, size, sort 조건을 Repository에 넘길 수 있는 객체로 변환
        Pageable pageable = createPageable(page, normalizedSize, sort);

        // 검색 조건 조합
        // OrderSpecification에 분리해둔 조건들을 조립
        // null인 조건은 Specification에서 제외됨
        Specification<Order> spec = Specification.<Order>unrestricted()
                .and(userIdEquals(currentUserId))   // 본인 주문만 조회
                .and(deletedAtIsNull())               // Soft Delete 제외
                .and(statusEquals(status))            // status가 있을 때만 상태 조건 추가
                .and(createdAtGoe(startDateTime))     // startDate가 있을 때만 시작일 조건 추가
                .and(createdAtLoe(endDateTime));      // endDate가 있을 때만 종료일 조건 추가

        // DB 조회
        // Specification = 검색 조건
        // Pageable = 페이징 + 정렬 조건
        Page<Order> orders = orderRepository.findAll(spec, pageable);

        // Page<Order>를 응답 DTO로 변환
        return OrderListResponse.from(orders);
    }


    // 가게 주문 내역 조회
    @Transactional(readOnly = true)
    public OrderListResponse getStoreOrders(
            UUID storeId,
            LocalDate startDate,
            LocalDate endDate,
            OrderStatus status,
            int page,
            int size,
            String sort
    ) {
        // TODO: Store 도메인 연동 후 storeId 존재 여부 검증
        // 예: storeRepository.existsById(storeId) 또는 storeService.validateStoreExists(storeId)

        // TODO: Spring Security/JWT 및 Store 도메인 연동 후 접근 권한 검증
        // OWNER: 로그인한 사용자가 해당 storeId의 소유자인 경우만 조회 가능
        // MANAGER / MASTER: 전체 가게 주문 조회 가능
        // 현재는 Store/User 도메인이 연결 전이라 storeId 조건 검색만 수행

        validateDateRange(startDate, endDate);

        LocalDateTime startDateTime = startDate != null
                ? startDate.atStartOfDay()
                : null;

        LocalDateTime endDateTime = endDate != null
                ? endDate.atTime(LocalTime.MAX)
                : null;

        int normalizedSize = normalizePageSize(size);
        Pageable pageable = createPageable(page, normalizedSize, sort);

        // storeId 기준으로 본인 가게 주문만 조회
        // 날짜, 상태 조건은 값이 있을 때만 추가됨
        Specification<Order> spec = Specification.<Order>unrestricted()
                .and(storeIdEquals(storeId))
                .and(deletedAtIsNull())
                .and(statusEquals(status))
                .and(createdAtGoe(startDateTime))
                .and(createdAtLoe(endDateTime));

        Page<Order> orders = orderRepository.findAll(spec, pageable);

        return OrderListResponse.from(orders);
    }


    // 날짜 범위 검증 메서드
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(OrderErrorCode.INVALID_ORDER_DATE_RANGE);
        }
    }

    // 페이지 크기 보정 메서드
    private int normalizePageSize(int size) {
        if (size == 10 || size == 30 || size == 50) {
            return size;
        }

        return 10;
    }

    private Pageable createPageable(int page, int size, String sort) {
        // 기본 정렬은 생성일 기준 최신순
        Sort.Direction direction = Sort.Direction.DESC;

        // sort=createdAt,asc인 경우에만 오래된순으로 변경
        if ("createdAt,asc".equalsIgnoreCase(sort)) {
            direction = Sort.Direction.ASC;
        }

        return PageRequest.of(
                Math.max(page, 0),
                size,
                Sort.by(direction, "createdAt")
        );
    }


    // 관리자 주문 삭제(Soft Delete)
    @Transactional
    public void deleteOrder(UUID orderId, Long currentAdminId) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() ->
                        new BusinessException(OrderErrorCode.ORDER_NOT_FOUND)
                );

        // TODO: Spring Security/JWT 연동 후 MANAGER, MASTER 권한 검증
        // 현재는 관리자 권한 검증 전이므로 Soft Delete 동작만 우선 확인
        // 현재 currentAdminId 임시값이며, 추후 인증된 관리자 ID로 교체

        // BaseEntity의 delete()를 활용
        // 실제 DELETE가 아니라 deleted_at, deleted_by 값을 채우는 Soft Delete
        order.delete(String.valueOf(currentAdminId));
    }




    // Customer 검증 메서드
    private void validateOrderAccessForCustomer(Order order, Long currentUserId) {
        if (!order.getUserId().equals(currentUserId)) {
            throw new BusinessException(OrderErrorCode.FORBIDDEN_ORDER_ACCESS);
        }
    }


    // 가게 존재 여부 확인하는 메서드
    private void validateStoreExists(UUID storeId) {
        // TODO: 가게 도메인 Repository 또는 Service 확정 후 구현
        // 현재는 주문 생성 API 흐름 테스트를 위해 통과 처리

        /* 예시:
         boolean exists = storeRepository.existsById(storeId);
         if (!exists) {
             throw new BusinessException(OrderErrorCode.STORE_NOT_FOUND);
         }*/
    }

    // 메뉴 스냅샷 저장
    private MenuSnapshot getMenuSnapshot(UUID storeId, UUID menuId) {
        // TODO: 메뉴 도메인 Repository 또는 Service 확정 후 구현
        // TODO: 메뉴 도메인 Repository 또는 Service 확정 후 반드시 실제 조회 로직으로 교체
        // 현재는 주문 생성 API 흐름 테스트를 위해 임시 메뉴 스냅샷을 반환

        return new MenuSnapshot(
                menuId,
                "테스트 메뉴",
                10000
        );

        // 구현해야 할 내용:
        // 1. menuId로 메뉴 조회
        // 2. 메뉴가 없으면 MENU_NOT_FOUND 예외 발생
        // 3. 메뉴의 storeId와 요청 storeId 비교
        // 4. 다르면 MENU_STORE_MISMATCH 예외 발생
        // 5. menuId, menuName, menuPrice 반환

//        throw new UnsupportedOperationException("메뉴 도메인 연결 후 구현 필요");


    }

    // 수량 검증 메서드
    private void validateOrderQuantity(Integer quantity) {
        // Request DTO에서 @Min으로 검증하더라도
        // 서비스 계층에서도 핵심 비즈니스 규칙은 한 번 더 보호할 수 있음
        if (quantity == null || quantity < 1) {
            throw new BusinessException(OrderErrorCode.INVALID_ORDER_QUANTITY);
        }
    }

    // 주문 도메인에서 메뉴 도메인으로부터 필요한 값만 담는 내부 DTO
    // 실제 Menu Entity 구조를 몰라도 주문 생성 흐름을 먼저 작성할 수 있게 해줌
    private record MenuSnapshot(
            UUID menuId,
            String menuName,
            Integer menuPrice
    ) {
    }

}
