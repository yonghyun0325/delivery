package com.delivery.domain.payment.dto.response;

import com.delivery.domain.payment.entity.Payment;
import com.delivery.domain.payment.entity.PaymentMethod;
import com.delivery.domain.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        @Schema(description = "결제 ID", example = "55555555-5555-5555-5555-555555555555")
                UUID paymentId,
        @Schema(description = "주문 ID", example = "44444444-4444-4444-4444-444444444444")
                UUID orderId,
        @Schema(description = "결제 소유 사용자 ID", example = "2") Long userId,
        @Schema(description = "결제 상태") PaymentStatus paymentStatus,
        @Schema(description = "결제 수단") PaymentMethod paymentMethod,
        @Schema(description = "결제 금액", example = "32000") Integer paymentAmount,
        @Schema(description = "결제 시각") LocalDateTime paidAt,
        @Schema(description = "취소 시각") LocalDateTime canceledAt,
        @Schema(description = "취소 사유", nullable = true) String cancelReason) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getPaymentStatus(),
                payment.getPaymentMethod(),
                payment.getPaymentAmount(),
                payment.getPaidAt(),
                payment.getCanceledAt(),
                payment.getCancelReason());
    }
}
