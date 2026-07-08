package com.delivery.domain.payment.dto.response;

import com.delivery.domain.payment.entity.Payment;
import com.delivery.domain.payment.entity.PaymentMethod;
import com.delivery.domain.payment.entity.PaymentStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID orderId,
        Long userId,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        Integer paymentAmount,
        LocalDateTime paidAt,
        LocalDateTime canceledAt,
        String cancelReason) {

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
