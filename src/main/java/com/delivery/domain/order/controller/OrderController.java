package com.delivery.domain.order.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.order.dto.request.OrderCreateRequest;
import com.delivery.domain.order.dto.response.OrderCreateResponse;
import com.delivery.domain.order.dto.response.OrderDetailResponse;
import com.delivery.domain.order.dto.response.OrderListResponse;
import com.delivery.domain.order.dto.response.OrderStatusResponse;
import com.delivery.domain.order.enums.OrderStatus;
import com.delivery.domain.order.service.OrderService;
import com.delivery.global.security.config.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController implements OrderControllerDocs {

    private final OrderService orderService;

    // 고객 주문 생성
    @Override
    @PostMapping("/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<RestApiResponse<OrderCreateResponse>> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        // JWT 인증이 완료된 현재 로그인 고객의 ID 사용
        Long currentUserId = userDetails.getId();

        OrderCreateResponse response = orderService.createOrder(request, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestApiResponse.success(
                        HttpStatus.CREATED,
                        "주문이 생성되었습니다.",
                        response
                ));

    }

    // 주문 단건 조회
    @Override
    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'OWNER', 'MANAGER', 'MASTER')")
    public ResponseEntity<RestApiResponse<OrderDetailResponse>> getOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ){

        // 현재 로그인 사용자 ID를 Service에 전달
        Long currentUserId = userDetails.getId();

        // 역할별 주문 접근 범위를 Service에서 검증하기 위해 현재 사용자의 권한 목록도 함께 전달
        Set<String> currentRoles = userDetails.getRoleNames();

        OrderDetailResponse response = orderService.getOrder(
                orderId,
                currentUserId,
                currentRoles
        );

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "주문 조회에 성공했습니다.",
                        response
                )
        );

    }

    // 고객 본인 주문 내역 조회
    @Override
    @GetMapping("/orders/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<RestApiResponse<OrderListResponse>> getMyOrders(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false)
            OrderStatus status,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "createdAt,desc")
            String sort,

            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // JWT 인증이 완료된 현재 로그인 고객 ID
        Long currentUserId = userDetails.getId();

        OrderListResponse response = orderService.getMyOrders(
                currentUserId,
                startDate,
                endDate,
                status,
                page,
                size,
                sort
        );

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "주문 내역 조회에 성공했습니다.",
                        response
                )
        );
    }


    // 가게 주문 내역 조회
    @Override
    @GetMapping("/stores/{storeId}/orders")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    public ResponseEntity<RestApiResponse<OrderListResponse>> getStoreOrders(
            @PathVariable UUID storeId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false)
            OrderStatus status,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "createdAt,desc")
            String sort,

            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        // 현재 로그인 사용자 ID
        Long currentUserId = userDetails.getId();

        // 현재 로그인 사용자가 가진 역할 목록 추출
        // 예: ROLE_OWNER, ROLE_MANAGER, ROLE_MASTER
        Set<String> currentRoles = userDetails.getRoleNames();


        OrderListResponse response = orderService.getStoreOrders(
                storeId,
                currentUserId,
                currentRoles,
                startDate,
                endDate,
                status,
                page,
                size,
                sort
        );

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "가게 주문 내역 조회에 성공했습니다.",
                        response
                )
        );
    }

    // 관리자 주문 삭제
    @Override
    @DeleteMapping("/admin/orders/{orderId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
    public ResponseEntity<RestApiResponse<Void>> deleteOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 실제 로그인 관리자 ID
        Long currentAdminId = userDetails.getId();

        // 관리자 ID와 삭제 대상 주문 ID를 Service에 전달
        orderService.deleteOrder(orderId, currentAdminId);

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "주문이 삭제되었습니다.",
                        null
                )
        );
    }


    // 주문 상태 변경 7가지
    // 고객 주문 취소 or 가게 주문 거절
    // 가게 주문 수락 -> 조리중 -> 배달 중 -> 배달 완료 -> 주문 최종 완료(리뷰 가능)

    // 고객 주문 취소
    @Override
    @PatchMapping("/orders/{orderId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<RestApiResponse<OrderStatusResponse>> cancelOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 실제 로그인 고객 ID
        Long currentUserId = userDetails.getId();

        OrderStatusResponse response =
                orderService.cancelOrder(orderId, currentUserId);

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "주문이 취소되었습니다.",
                        response
                )
        );
    }

    // 가게 주문 거절
    @Override
    @PatchMapping("/stores/{storeId}/orders/{orderId}/reject")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    public ResponseEntity<RestApiResponse<OrderStatusResponse>> rejectOrder(
            @PathVariable UUID storeId,
            @PathVariable UUID orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 현재 로그인한 가게 처리 담당 사용자 ID
        Long currentUserId = userDetails.getId();

        // 현재 로그인 사용자가 가진 역할 목록
        // OWNER 소유권 검증 또는 MANAGER/MASTER 예외 처리를 위해 역할 전달
        Set<String> currentRoles = userDetails.getRoleNames();

        OrderStatusResponse response =
                orderService.changeStoreOrderStatus(
                        storeId,
                        orderId,
                        OrderStatus.REJECTED,
                        currentUserId,
                        currentRoles
                );

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "주문이 거절되었습니다.",
                        response
                )
        );
    }


    // 가게 주문 수락
    @Override
    @PatchMapping("/stores/{storeId}/orders/{orderId}/accept")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    public ResponseEntity<RestApiResponse<OrderStatusResponse>> acceptOrder(
            @PathVariable UUID storeId,
            @PathVariable UUID orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 현재 로그인한 가게 처리 담당 사용자 ID
        Long currentUserId = userDetails.getId();

        // 현재 로그인 사용자가 가진 역할 목록
        // OWNER 소유권 검증 또는 MANAGER/MASTER 예외 처리를 위해 역할 전달
        Set<String> currentRoles = userDetails.getRoleNames();

        OrderStatusResponse response =
                orderService.changeStoreOrderStatus(
                        storeId,
                        orderId,
                        OrderStatus.ACCEPTED,
                        currentUserId,
                        currentRoles
                );

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "주문이 수락되었습니다.",
                        response
                )
        );
    }

    // 가게 조리 중 변경
    @Override
    @PatchMapping("/stores/{storeId}/orders/{orderId}/cook")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    public ResponseEntity<RestApiResponse<OrderStatusResponse>> startCooking(
            @PathVariable UUID storeId,
            @PathVariable UUID orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 현재 로그인한 가게 처리 담당 사용자 ID
        Long currentUserId = userDetails.getId();

        // 현재 로그인 사용자가 가진 역할 목록
        // OWNER 소유권 검증 또는 MANAGER/MASTER 예외 처리를 위해 역할 전달
        Set<String> currentRoles = userDetails.getRoleNames();

        OrderStatusResponse response =
                orderService.changeStoreOrderStatus(
                        storeId,
                        orderId,
                        OrderStatus.COOKING,
                        currentUserId,
                        currentRoles
                );

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "주문 상태가 조리 중으로 변경되었습니다.",
                        response
                )
        );
    }

    // 배달 중 변경
    @Override
    @PatchMapping("/stores/{storeId}/orders/{orderId}/deliver")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    public ResponseEntity<RestApiResponse<OrderStatusResponse>> startDelivery(
            @PathVariable UUID storeId,
            @PathVariable UUID orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        // 현재 로그인한 가게 처리 담당 사용자 ID
        Long currentUserId = userDetails.getId();

        // 현재 로그인 사용자가 가진 역할 목록
        // OWNER 소유권 검증 또는 MANAGER/MASTER 예외 처리를 위해 역할 전달
        Set<String> currentRoles = userDetails.getRoleNames();

        OrderStatusResponse response =
                orderService.changeStoreOrderStatus(
                        storeId,
                        orderId,
                        OrderStatus.DELIVERING,
                        currentUserId,
                        currentRoles
                );

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "주문 상태가 배달 중으로 변경되었습니다.",
                        response
                )
        );
    }

    // 배달 완료 변경
    @Override
    @PatchMapping("/stores/{storeId}/orders/{orderId}/delivered")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    public ResponseEntity<RestApiResponse<OrderStatusResponse>> completeDelivery(
            @PathVariable UUID storeId,
            @PathVariable UUID orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        // 현재 로그인한 가게 처리 담당 사용자 ID
        Long currentUserId = userDetails.getId();

        // 현재 로그인 사용자가 가진 역할 목록
        // OWNER 소유권 검증 또는 MANAGER/MASTER 예외 처리를 위해 역할 전달
        Set<String> currentRoles = userDetails.getRoleNames();

        OrderStatusResponse response =
                orderService.changeStoreOrderStatus(
                        storeId,
                        orderId,
                        OrderStatus.DELIVERED,
                        currentUserId,
                        currentRoles
                );

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "주문 상태가 배달 완료로 변경되었습니다.",
                        response
                )
        );
    }


    // 고객 주문 최종 완료
    @Override
    @PatchMapping("/orders/{orderId}/complete")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<RestApiResponse<OrderStatusResponse>> completeOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 실제 로그인 고객 ID
        Long currentUserId = userDetails.getId();

        OrderStatusResponse response =
                orderService.completeOrder(orderId, currentUserId);

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "주문이 최종 완료되었습니다.",
                        response
                )
        );
    }


}
