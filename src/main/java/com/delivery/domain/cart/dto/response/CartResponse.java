package com.delivery.domain.cart.dto.response;

import com.delivery.domain.cart.entity.Cart;
import com.delivery.domain.cart.entity.CartStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "내 장바구니 응답")
public record CartResponse(
        @Schema(
                        description = "장바구니 ID",
                        nullable = true,
                        example = "77777777-7777-7777-7777-777777777777")
                UUID cartId,
        @Schema(description = "사용자 ID", nullable = true, example = "1") Long userId,
        @Schema(
                        description = "가게 ID",
                        nullable = true,
                        example = "88888888-8888-8888-8888-888888888888")
                UUID storeId,
        @Schema(description = "장바구니 상태", nullable = true) CartStatus cartStatus,
        @Schema(description = "장바구니 항목 목록") List<CartItemResponse> items,
        @Schema(description = "총 수량", example = "2") int totalQuantity,
        @Schema(description = "총 금액", example = "24000") long totalPrice) {

    public static CartResponse from(Cart cart, List<CartItemResponse> items) {
        int totalQuantity = items.stream().mapToInt(CartItemResponse::quantity).sum();
        long totalPrice = items.stream().mapToLong(CartItemResponse::subtotalPrice).sum();

        return new CartResponse(
                cart.getCartId(),
                cart.getUserId(),
                cart.getStoreId(),
                cart.getCartStatus(),
                items,
                totalQuantity,
                totalPrice);
    }

    public static CartResponse empty(Long userId) {
        return new CartResponse(null, userId, null, null, List.of(), 0, 0L);
    }
}
