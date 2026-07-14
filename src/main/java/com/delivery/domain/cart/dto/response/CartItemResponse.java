package com.delivery.domain.cart.dto.response;

import com.delivery.domain.cart.entity.CartItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "장바구니 항목 응답")
public record CartItemResponse(
        @Schema(description = "장바구니 항목 ID", example = "66666666-6666-6666-6666-666666666666")
                UUID cartItemId,
        @Schema(description = "메뉴 ID", example = "55555555-5555-5555-5555-555555555555")
                UUID menuId,
        @Schema(description = "메뉴명", example = "김치찌개") String menuName,
        @Schema(description = "수량", example = "2") Integer quantity,
        @Schema(description = "메뉴 가격 스냅샷", example = "12000") Long menuPrice,
        @Schema(description = "항목 합계 금액", example = "24000") Long subtotalPrice) {

    public static CartItemResponse from(CartItem cartItem) {
        return new CartItemResponse(
                cartItem.getCartItemId(),
                cartItem.getMenuId(),
                cartItem.getMenuNameSnapshot(),
                cartItem.getQuantity(),
                cartItem.getMenuPriceSnapshot(),
                cartItem.getSubtotalPrice());
    }
}
