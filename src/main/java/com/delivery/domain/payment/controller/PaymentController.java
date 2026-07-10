package com.delivery.domain.payment.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.payment.dto.request.PaymentCancelRequest;
import com.delivery.domain.payment.dto.response.PaymentPageResponse;
import com.delivery.domain.payment.dto.response.PaymentResponse;
import com.delivery.domain.payment.entity.PaymentStatus;
import com.delivery.domain.payment.service.PaymentService;
import com.delivery.global.security.config.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/payments/{paymentId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'OWNER', 'MANAGER', 'MASTER')")
    public ResponseEntity<RestApiResponse<PaymentResponse>> getPayment(
            @PathVariable UUID paymentId, @AuthenticationPrincipal CustomUserDetails userDetail) {
        PaymentResponse response = paymentService.getPayment(paymentId, userDetail);
        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "결제 상세 조회에 성공했습니다.", response));
    }

    @GetMapping("/payments/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<RestApiResponse<PaymentPageResponse>> getMyPayments(
            @AuthenticationPrincipal CustomUserDetails userDetail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) PaymentStatus status) {
        PaymentPageResponse response = paymentService.getMyPayments(userDetail, page, size, status);
        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "내 결제 목록 조회에 성공했습니다.", response));
    }

    @GetMapping("/owner/stores/{storeId}/payments")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    public ResponseEntity<RestApiResponse<PaymentPageResponse>> getStorePayments(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal CustomUserDetails userDetail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) PaymentStatus status) {
        PaymentPageResponse response =
                paymentService.getStorePayments(storeId, userDetail, page, size, status);
        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "가게 결제 목록 조회에 성공했습니다.", response));
    }

    @PatchMapping("/payments/{paymentId}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER', 'MASTER')")
    public ResponseEntity<RestApiResponse<PaymentResponse>> cancelPayment(
            @PathVariable UUID paymentId,
            @Valid @RequestBody PaymentCancelRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetail) {
        PaymentResponse response =
                paymentService.cancelPayment(paymentId, request.cancelReason(), userDetail);
        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "결제 취소에 성공했습니다.", response));
    }
}
