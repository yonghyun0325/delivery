package com.delivery.domain.order.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.order.dto.request.OrderCreateRequest;
import com.delivery.domain.order.dto.response.OrderCreateResponse;
import com.delivery.domain.order.dto.response.OrderDetailResponse;
import com.delivery.domain.order.enums.OrderStatus;
import com.delivery.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 고객 주문 생성
    @PostMapping
    public ResponseEntity<RestApiResponse<OrderCreateResponse>> createOrder(
            @Valid @RequestBody OrderCreateRequest request
    ){
        // TODO: Spring Security/JWT 적용 후 인증 객체에서 로그인 사용자 ID 가져오기

        Long currentUserId = 2L;

        OrderCreateResponse response = orderService.createOrder(request, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestApiResponse.success(
                        HttpStatus.CREATED,
                        "주문이 생성되었습니다.",
                        response
                ));

    }

    // 주문 단건 조회
    @GetMapping("/{orderId}")
    // TODO: Spring Security/JWT 연동 후 역할 기반 접근 권한 적용
//    @PreAuthorize("hasAnyRole('CUSTOMER', 'OWNER', 'MANAGER', 'MASTER')")
    public ResponseEntity<RestApiResponse<OrderDetailResponse>> getOrder(
            @PathVariable UUID orderId
            ){
        // TODO: Spring Security/JWT 적용 후 인증 객체에서 로그인 사용자 ID 추출
        Long currentUserId = 1L;

        OrderDetailResponse response = orderService.getOrder(orderId, currentUserId);

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "주문 조회에 성공했습니다.",
                        response
                )
        );

    }



}
