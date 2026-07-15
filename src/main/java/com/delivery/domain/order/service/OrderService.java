package com.delivery.domain.order.service;

import static com.delivery.domain.order.repository.OrderSpecification.*;

import com.delivery.domain.menu.dto.response.MenuSnapshot;
import com.delivery.domain.menu.service.MenuService;
import com.delivery.domain.order.dto.request.OrderCreateRequest;
import com.delivery.domain.order.dto.request.OrderItemCreateRequest;
import com.delivery.domain.order.dto.response.OrderCreateResponse;
import com.delivery.domain.order.dto.response.OrderDetailResponse;
import com.delivery.domain.order.dto.response.OrderListResponse;
import com.delivery.domain.order.dto.response.OrderStatusResponse;
import com.delivery.domain.order.entity.Order;
import com.delivery.domain.order.entity.OrderItem;
import com.delivery.domain.order.enums.OrderStatus;
import com.delivery.domain.order.exception.OrderErrorCode;
import com.delivery.domain.order.exception.OrderException;
import com.delivery.domain.order.repository.OrderRepository;
import com.delivery.domain.payment.entity.PaymentMethod;
import com.delivery.domain.payment.service.PaymentService;
import com.delivery.domain.store.entity.Store;
import com.delivery.domain.store.repository.StoreRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final StoreRepository storeRepository;

    private final MenuService menuService;

    private final PaymentService paymentService;

    // 고객 주문 생성
    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request, Long currentUserId) {
        // 요청한 storeId로 실제 존재하고 삭제되지 않은 가게를 조회
        // 존재하지 않거나 Soft Delete된 가게이면 STORE_NOT_FOUND 예외 발생
        Store store = findActiveStore(request.storeId());

        // 가게 영업 여부 확인
        validateStoreOpen(store);

        // 주문 엔티티 생성
        // userId는 요청값이 아니라 JWT에서 가져온 로그인 사용자 ID를 사용
        // status는 생성 시 기본값 REQUESTED로 설정됨
        Order order = new Order(currentUserId, request.storeId(), request.deliveryAddress());

        // 요청에 담긴 주문 메뉴 목록을 하나씩 처리
        for (OrderItemCreateRequest itemRequest : request.items()) {

            // 수량 검증 (수량은 1개 이상)
            validateOrderQuantity(itemRequest.quantity());

            // 메뉴 존재, 소속 가게, 노출 여부, 가격 검증
            // 검증된 메뉴의 주문 당시 정보를 스냅샷으로 반환
            MenuSnapshot menuSnapshot =
                    menuService.getOrderableMenu(itemRequest.menuId(), request.storeId());

            // 주문 상세 엔티티 생성
            // 주문 당시 메뉴명(menuName)과 가격(menuPrice)으로 주문 상세 생성
            OrderItem orderItem =
                    new OrderItem(
                            menuSnapshot.menuId(),
                            menuSnapshot.name(),
                            menuSnapshot.price(),
                            itemRequest.quantity());

            // 주문에 주문 상세 추가
            /* addOrderItem 내부에서 Order와 OrderItem의 연관관계를 연결하고,
            totalPrice에 subtotalPrice를 누적함*/
            order.addOrderItem(orderItem);
        }

        // 모든 메뉴 금액이 합산된 후 최소 주문 금액 검증
        validateMinimumOrderAmount(order, store);

        // 주문 저장
        // Order에 Cascade 설정이 있어 OrderItem도 함께 저장됨
        Order savedOrder = orderRepository.save(order);

        paymentService.createPayment(
                savedOrder.getId(), currentUserId, savedOrder.getTotalPrice(), PaymentMethod.CARD);

        // 저장된 주문 엔티티를 응답 DTO로 변환
        return OrderCreateResponse.from(savedOrder);
    }

    // 주문 단건 조회
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(
            UUID orderId, Long currentUserId, Set<String> currentRoles) {
        // 삭제되지 않은 주문 주회
        // 주문 단건 응답에 메뉴 상세 목록이 포함되므로 orderItems도 함께 조회
        Order order =
                orderRepository
                        .findByIdAndDeletedAtIsNull(orderId)
                        .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // 현재 사용자의 역할(role)과 주문 소유 관계(id)에 따라 접근 가능 여부 검증
        validateOrderDetailAccess(order, currentUserId, currentRoles);

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
            String sort) {
        // 날짜 범위 검증
        // startDate가 endDate보다 늦으면 잘못된 조회 조건으로 판단
        validateDateRange(startDate, endDate);

        // LocalDate를 LocalDateTime으로 변환
        // startDate는 해당 날짜의 00:00:00부터 조회
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;

        // endDate는 해당 날짜의 마지막 시간까지 조회
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        // 페이지 크기 보정
        // 과제 조건에 따라 10, 30, 50만 허용하고 그 외 값은 10으로 고정
        int normalizedSize = normalizePageSize(size);

        // Pageable 생성
        // page, size, sort 조건을 Repository에 넘길 수 있는 객체로 변환
        Pageable pageable = createPageable(page, normalizedSize, sort);

        // 검색 조건 조합
        // OrderSpecification에 분리해둔 조건들을 조립
        // null인 조건은 Specification에서 제외됨
        Specification<Order> spec =
                Specification.<Order>unrestricted()
                        .and(userIdEquals(currentUserId)) // 본인 주문만 조회
                        .and(deletedAtIsNull()) // Soft Delete 제외
                        .and(statusEquals(status)) // status가 있을 때만 상태 조건 추가
                        .and(createdAtGoe(startDateTime)) // startDate가 있을 때만 시작일 조건 추가
                        .and(createdAtLoe(endDateTime)); // endDate가 있을 때만 종료일 조건 추가

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
            Long currentUserId,
            Set<String> currentRoles,
            LocalDate startDate,
            LocalDate endDate,
            OrderStatus status,
            int page,
            int size,
            String sort) {

        // URL로 받은 storeId의 가게가 실제 존재하고 삭제되지 않았는지 확인
        Store store = findActiveStore(storeId);

        // 역할별 가게 접근 권한 검증
        // 현재 로그인한 OWNER가 해당 가게의 실제 소유자인지 확인(OWNER는 본인 가게만 가능)
        // Store.userId == JWT 로그인 사용자 ID인 경우만 상태 변경 허용
        // MANAGER / MASTER는 모든 가게 가능
        validateStoreAccess(store, currentUserId, currentRoles);

        // 검색 시작일과 종료일 범위 검증
        validateDateRange(startDate, endDate);

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;

        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        int normalizedSize = normalizePageSize(size);
        Pageable pageable = createPageable(page, normalizedSize, sort);

        // 검증이 완료된 storeId의 주문만 검색(조회)
        // storeId 기준으로 본인 가게 주문만 조회
        // 날짜, 상태 조건은 값이 있을 때만 추가됨
        Specification<Order> spec =
                Specification.<Order>unrestricted()
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
            throw new OrderException(OrderErrorCode.INVALID_ORDER_DATE_RANGE);
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

        return PageRequest.of(Math.max(page, 0), size, Sort.by(direction, "createdAt"));
    }

    // 관리자 주문 삭제(Soft Delete)
    @Transactional
    public void deleteOrder(UUID orderId, Long currentAdminId) {
        Order order =
                orderRepository
                        .findByIdAndDeletedAtIsNull(orderId)
                        .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // TODO: Spring Security/JWT 연동 후 MANAGER, MASTER 권한 검증
        // MANAGER, MASTER 권한으로 회원가입을 할 수 없어 우선 보류

        // 현재는 관리자 권한 검증 전이므로 Soft Delete 동작만 우선 확인
        // 현재 currentAdminId 임시값이며, 추후 인증된 관리자 ID로 교체

        // BaseEntity의 delete()를 활용
        // 실제 DELETE가 아니라 deleted_at, deleted_by 값을 채우는 Soft Delete
        order.delete(String.valueOf(currentAdminId));
    }

    // 고객 주문 상태 변경
    // 주문 취소(고객)
    @Transactional
    public OrderStatusResponse cancelOrder(UUID orderId, Long currentUserId) {
        // 삭제되지 않은 주문 조회
        Order order = findActiveOrder(orderId);

        // 고객 본인 주문인지 검증
        validateOrderAccessForCustomer(order, currentUserId);

        // REQUESTED 상태에서만 취소 가능
        validateStatusTransition(order, OrderStatus.CUSTOMER_CANCELLED);

        // 주문 생성 후 5분 이내인지 검증
        validateCancelTime(order);

        // Soft Delete가 아니라 주문 상태를 CUSTOMER_CANCELLED로 변경
        order.changeStatus(OrderStatus.CUSTOMER_CANCELLED);

        // 변경된 상태 응답
        return OrderStatusResponse.from(order);
    }

    // 주문 최종 완료(고객)
    @Transactional
    public OrderStatusResponse completeOrder(UUID orderId, Long currentUserId) {
        // 삭제되지 않은 주문 조회
        Order order = findActiveOrder(orderId);

        // 고객 본인 주문인지 검증
        validateOrderAccessForCustomer(order, currentUserId);

        // DELIVERED 상태에서만 COMPLETED로 변경 가능
        validateStatusTransition(order, OrderStatus.COMPLETED);

        // 주문 상태 변경
        order.changeStatus(OrderStatus.COMPLETED);

        return OrderStatusResponse.from(order);
    }

    // 가게 주문 상태 변경
    @Transactional
    public OrderStatusResponse changeStoreOrderStatus(
            UUID storeId,
            UUID orderId,
            OrderStatus nextStatus,
            Long currentUserId,
            Set<String> currentRoles) {
        // 삭제되지 않은 주문 조회
        Order order = findActiveOrder(orderId);

        // 요청 URL의 storeId와 실제 주문의 storeId가 같은지 확인
        // 다른 가게의 주문 ID를 URL에 넣은 경우 차단
        validateOrderBelongsToStore(order, storeId);

        // URL로 받은 storeId의 가게가 실제 존재하는지 확인
        Store store = findActiveStore(storeId);

        // 역할별 가게 접근 권한 검증
        // 현재 로그인한 OWNER가 해당 가게의 실제 소유자인지 확인(OWNER는 본인 가게만 가능)
        // Store.userId == JWT 로그인 사용자 ID인 경우만 상태 변경 허용
        // MANAGER / MASTER는 모든 가게 가능
        validateStoreAccess(store, currentUserId, currentRoles);

        // 현재 상태에서 요청한 다음 상태로 변경 가능한지 확인
        // 상태 전이 가능 여부 검증
        validateStatusTransition(order, nextStatus);

        // 모든 검증을 통과하면 주문 상태 변경
        if (nextStatus == OrderStatus.REJECTED) {
            paymentService.refundPaymentByStoreRejection(
                    order.getId(), "가게가 주문을 거절하여 자동 환불되었습니다.");
        }

        order.changeStatus(nextStatus);

        return OrderStatusResponse.from(order);
    }

    // 삭제되지 않은 주문 조회 메서드 (공통)
    private Order findActiveOrder(UUID orderId) {
        return orderRepository
                .findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
    }

    // 삭제되지 않은 가게 조회 메서드
    private Store findActiveStore(UUID storeId) {
        // storeId에 해당하는 가게가 존재하고 Soft Delete되지 않았는지 조회
        return storeRepository
                .findByStoreIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.STORE_NOT_FOUND));
    }

    // 현재 로그인한 사용자가 해당 가게의 실제 소유자인지 검증
    /*private void validateStoreOwner(Store store, Long currentUserId) {
        // Store.userId는 해당 가게를 소유한 OWNER의 사용자 ID
        // currentUserId는 JWT 인증 객체에서 가져온 현재 로그인 사용자 ID
        if (!store.getUserId().equals(currentUserId)) {
            throw new OrderException(
                    OrderErrorCode.FORBIDDEN_STORE_ACCESS
            );
        }
    }*/

    // 역할별 가게 접근 권한 검증
    // OWNER - 본인이 소유한 가게만 접근 가능
    // MANAGER / MASTER - 가게 소유자 ID와 관계없이 모든 가게 접근 가능
    private void validateStoreAccess(Store store, Long currentUserId, Set<String> currentRoles) {
        // MANAGER 또는 MASTER라면 소유권 검증 없이 접근 허용
        boolean isAdmin =
                currentRoles.contains("ROLE_MANAGER") || currentRoles.contains("ROLE_MASTER");

        if (isAdmin) {
            return;
        }

        // 관리자가 아니라면 OWNER 본인 가게인지 확인
        if (!store.getUserId().equals(currentUserId)) {
            throw new OrderException(OrderErrorCode.FORBIDDEN_STORE_ACCESS);
        }
    }

    // 주문 단건 조회 접근 권한 검증
    /* CUSTOMER - 본인이 생성한 주문만 조회 가능
     * OWNER - 본인이 소유한 가게의 주문만 조회 가능
     * MANAGER / MASTER - 소유권과 관계없이 전체 주문 조회 가능
     */
    private void validateOrderDetailAccess(
            Order order, Long currentUserId, Set<String> currentRoles) {
        // MANAGER 또는 MASTER는 전체 주문 조회 가능
        boolean isAdmin =
                currentRoles.contains("ROLE_MANAGER") || currentRoles.contains("ROLE_MASTER");

        if (isAdmin) {
            return;
        }

        // CUSTOMER는 본인이 생성한 주문이면 조회 가능
        boolean isOwnOrder =
                currentRoles.contains("ROLE_CUSTOMER") && order.getUserId().equals(currentUserId);

        if (isOwnOrder) {
            return;
        }

        // OWNER라면 주문이 속한 가게를 조회한 뒤 소유자 여부 확인
        if (currentRoles.contains("ROLE_OWNER")) {
            Store store = findActiveStore(order.getStoreId());

            boolean isOwnStore = store.getUserId().equals(currentUserId);

            if (isOwnStore) {
                return;
            }
        }

        // 어떤 접근 조건도 충족하지 못하면 조회 차단
        throw new OrderException(OrderErrorCode.FORBIDDEN_ORDER_ACCESS);
    }

    // 주문이 해당 URL 가게의 주문인지 검증
    private void validateOrderBelongsToStore(Order order, UUID storeId) {
        if (!order.getStoreId().equals(storeId)) {
            throw new OrderException(OrderErrorCode.ORDER_STORE_MISMATCH);
        }
    }

    // 가게 영업 여부 검증
    private void validateStoreOpen(Store store) {
        if (!Boolean.TRUE.equals(store.getIsOpen())) {
            throw new OrderException(OrderErrorCode.STORE_NOT_OPEN);
        }
    }

    // 최소 주문 금액 검증
    private void validateMinimumOrderAmount(Order order, Store store) {
        if (order.getTotalPrice() < store.getMinOrderAmount()) {
            throw new OrderException(OrderErrorCode.MINIMUM_ORDER_AMOUNT_NOT_MET);
        }
    }

    // 주문 상태 전이 검증 (공통)
    private void validateStatusTransition(Order order, OrderStatus nextStatus) {

        // 현재 주문 상태
        OrderStatus currentStatus = order.getStatus();

        // 이미 종료된 주문은 더 이상 상태 변경 불가
        if (currentStatus == OrderStatus.COMPLETED
                || currentStatus == OrderStatus.REJECTED
                || currentStatus == OrderStatus.CUSTOMER_CANCELLED) {
            throw new OrderException(OrderErrorCode.ORDER_ALREADY_TERMINATED);
        }

        // 현재 상태에서 다음 상태로 변경 가능한지 확인
        boolean validTransition =
                switch (currentStatus) {

                        // 고객이 주문을 요청한 상태
                        // REQUESTED → ACCEPTED / REJECTED / CUSTOMER_CANCELLED 가능
                    case REQUESTED ->
                            nextStatus == OrderStatus.ACCEPTED
                                    || nextStatus == OrderStatus.REJECTED
                                    || nextStatus == OrderStatus.CUSTOMER_CANCELLED;

                        // 가게가 주문을 수락한 상태
                        // ACCEPTED → COOKING 가능
                    case ACCEPTED -> nextStatus == OrderStatus.COOKING;

                        // 조리 중
                        // COOKING → DELIVERING 가능
                    case COOKING -> nextStatus == OrderStatus.DELIVERING;

                        // 배달 중
                        // DELIVERING → DELIVERED 가능
                    case DELIVERING -> nextStatus == OrderStatus.DELIVERED;

                        // 배달 완료
                        // DELIVERED → COMPLETED 가능
                    case DELIVERED -> nextStatus == OrderStatus.COMPLETED;

                    default -> false;
                };

        // 허용되지 않은 상태 변경이면 예외 발생
        if (!validTransition) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_STATUS_TRANSITION);
        }
    }

    // 고객 주문 취소 5분 제한 검증
    private void validateCancelTime(Order order) {
        // 주문 생성 후 5분 이내에만 고객 취소 가능
        LocalDateTime cancelDeadline = order.getCreatedAt().plusMinutes(5);

        if (LocalDateTime.now().isAfter(cancelDeadline)) {
            throw new OrderException(OrderErrorCode.ORDER_CANCEL_TIME_EXPIRED);
        }
    }

    // Customer 검증 메서드
    private void validateOrderAccessForCustomer(Order order, Long currentUserId) {
        if (!order.getUserId().equals(currentUserId)) {
            throw new OrderException(OrderErrorCode.FORBIDDEN_ORDER_ACCESS);
        }
    }

    // 수량 검증 메서드
    private void validateOrderQuantity(Integer quantity) {
        // Request DTO에서 @Min으로 검증하더라도
        // 서비스 계층에서도 핵심 비즈니스 규칙은 한 번 더 보호할 수 있음
        if (quantity == null || quantity < 1) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_QUANTITY);
        }
    }

}
