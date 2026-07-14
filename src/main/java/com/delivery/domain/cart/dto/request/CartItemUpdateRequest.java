package com.delivery.domain.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "장바구니 항목 수량 수정 요청")
public record CartItemUpdateRequest(
        @NotNull(message = "REQUIRED_VALUE")
                @Min(value = 1, message = "BAD_REQUEST")
                @Schema(description = "변경할 수량", example = "3")
                Integer quantity) {}
