package com.delivery.domain.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record PaymentCancelRequest(
        @Schema(description = "결제 취소 사유", example = "고객 요청") @NotBlank(message = "REQUIRED_VALUE")
                String cancelReason) {}
