package com.delivery.domain.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "장바구니 항목 추가 요청")
public record CartItemCreateRequest(
        @NotNull(message = "REQUIRED_VALUE")
                @Schema(description = "메뉴 ID", example = "55555555-5555-5555-5555-555555555555")
                UUID menuId,
        @NotNull(message = "REQUIRED_VALUE")
                @Min(value = 1, message = "BAD_REQUEST")
                @Schema(description = "추가 수량", example = "2")
                Integer quantity) {}
