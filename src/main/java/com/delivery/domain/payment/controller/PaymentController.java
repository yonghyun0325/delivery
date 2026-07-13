package com.delivery.domain.payment.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.payment.dto.request.PaymentCancelRequest;
import com.delivery.domain.payment.dto.response.PaymentPageResponse;
import com.delivery.domain.payment.dto.response.PaymentResponse;
import com.delivery.domain.payment.entity.PaymentStatus;
import com.delivery.domain.payment.service.PaymentService;
import com.delivery.global.security.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "결제", description = "결제 조회 및 취소 API")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/payments/{paymentId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'OWNER', 'MANAGER', 'MASTER')")
    @Operation(summary = "결제 상세 조회", description = "CUSTOMER는 본인 결제만, OWNER는 본인 가게 주문 결제만, MANAGER/MASTER는 전체 결제를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "결제 상세 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "조회 권한 없음", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<RestApiResponse<PaymentResponse>> getPayment(
            @Parameter(description = "결제 ID", required = true) @PathVariable UUID paymentId,
            @AuthenticationPrincipal CustomUserDetails userDetail) {
        PaymentResponse response = paymentService.getPayment(paymentId, userDetail);
        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "결제 상세 조회에 성공했습니다.", response));
    }

    @GetMapping("/payments/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "내 결제 목록 조회", description = "CUSTOMER 본인 결제 목록을 페이지 단위로 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "내 결제 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 페이지 또는 상태값", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<RestApiResponse<PaymentPageResponse>> getMyPayments(
            @AuthenticationPrincipal CustomUserDetails userDetail,
            @Parameter(description = "페이지 번호(0부터 시작)") @RequestParam(defaultValue = "0")
                    int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "결제 상태 필터. PAID, CANCELED 중 하나", schema = @Schema(implementation = PaymentStatus.class))
            @RequestParam(required = false) PaymentStatus status) {
        PaymentPageResponse response = paymentService.getMyPayments(userDetail, page, size, status);
        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "내 결제 목록 조회에 성공했습니다.", response));
    }

    @GetMapping("/owner/stores/{storeId}/payments")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    @Operation(summary = "가게 결제 목록 조회", description = "OWNER는 본인 소유 가게 결제만, MANAGER/MASTER는 전체 가게 결제를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "가게 결제 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 페이지 또는 상태값", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "가게 결제 조회 권한 없음", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "가게 또는 결제를 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<RestApiResponse<PaymentPageResponse>> getStorePayments(
            @Parameter(description = "가게 ID", required = true) @PathVariable UUID storeId,
            @AuthenticationPrincipal CustomUserDetails userDetail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "결제 상태 필터. PAID, CANCELED 중 하나", schema = @Schema(implementation = PaymentStatus.class))
            @RequestParam(required = false) PaymentStatus status) {
        PaymentPageResponse response =
                paymentService.getStorePayments(storeId, userDetail, page, size, status);
        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "가게 결제 목록 조회에 성공했습니다.", response));
    }

    @PatchMapping("/payments/{paymentId}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER', 'MASTER')")
    @Operation(summary = "결제 취소", description = "CUSTOMER는 본인 결제만 취소할 수 있으며, OWNER는 취소할 수 없습니다. MANAGER/MASTER의 취소 허용 여부는 현재 구현 기준으로 허용됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "결제 취소 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 이미 취소된 결제", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "403", description = "결제 취소 권한 없음", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<RestApiResponse<PaymentResponse>> cancelPayment(
            @Parameter(description = "결제 ID", required = true) @PathVariable UUID paymentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "결제 취소 요청 본문",
                            required = true)
            @Valid @RequestBody PaymentCancelRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetail) {
        PaymentResponse response =
                paymentService.cancelPayment(paymentId, request.cancelReason(), userDetail);
        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "결제 취소에 성공했습니다.", response));
    }
}
