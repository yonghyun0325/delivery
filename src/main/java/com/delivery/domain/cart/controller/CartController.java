package com.delivery.domain.cart.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.cart.dto.request.CartItemCreateRequest;
import com.delivery.domain.cart.dto.request.CartItemUpdateRequest;
import com.delivery.domain.cart.dto.response.CartResponse;
import com.delivery.domain.cart.service.CartService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
@Tag(name = "장바구니", description = "장바구니 조회 및 항목 관리 API")
public class CartController {

    private final CartService cartService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "내 장바구니 조회", description = "CUSTOMER가 자신의 현재 장바구니를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "장바구니 조회 성공"),
        @ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<RestApiResponse<CartResponse>> getMyCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CartResponse response = cartService.getMyCart(userDetails);
        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "장바구니 조회에 성공했습니다.", response));
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "장바구니 항목 추가", description = "CUSTOMER가 메뉴를 장바구니에 추가합니다. 같은 메뉴는 수량이 합산됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "장바구니 항목 추가 성공"),
        @ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 또는 다른 가게 메뉴 추가",
                content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
                responseCode = "404",
                description = "메뉴를 찾을 수 없음",
                content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<RestApiResponse<CartResponse>> addCartItem(
            @Valid @RequestBody CartItemCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CartResponse response =
                cartService.addCartItem(userDetails, request.menuId(), request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestApiResponse.success(HttpStatus.CREATED, "장바구니 항목이 추가되었습니다.", response));
    }

    @PatchMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "장바구니 항목 수량 수정", description = "CUSTOMER가 자신의 장바구니 항목 수량을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "장바구니 항목 수정 성공"),
        @ApiResponse(
                responseCode = "400",
                description = "잘못된 수량 요청",
                content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
                responseCode = "403",
                description = "다른 사용자의 장바구니 항목 수정 불가",
                content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
                responseCode = "404",
                description = "장바구니 항목을 찾을 수 없음",
                content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<RestApiResponse<CartResponse>> updateCartItem(
            @Parameter(description = "장바구니 항목 ID", required = true) @PathVariable UUID cartItemId,
            @Valid @RequestBody CartItemUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CartResponse response =
                cartService.updateCartItem(userDetails, cartItemId, request.quantity());
        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "장바구니 항목이 수정되었습니다.", response));
    }

    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "장바구니 항목 삭제", description = "CUSTOMER가 자신의 장바구니 항목을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "장바구니 항목 삭제 성공"),
        @ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
                responseCode = "403",
                description = "다른 사용자의 장바구니 항목 삭제 불가",
                content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
                responseCode = "404",
                description = "장바구니 항목을 찾을 수 없음",
                content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<RestApiResponse<Void>> deleteCartItem(
            @Parameter(description = "장바구니 항목 ID", required = true) @PathVariable UUID cartItemId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        cartService.deleteCartItem(userDetails, cartItemId);
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "장바구니 항목이 삭제되었습니다.", null));
    }

    @DeleteMapping("/me/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "장바구니 비우기", description = "CUSTOMER가 자신의 장바구니 항목 전체를 비웁니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "장바구니 비우기 성공"),
        @ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
                responseCode = "404",
                description = "장바구니를 찾을 수 없음",
                content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<RestApiResponse<Void>> clearMyCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        cartService.clearMyCart(userDetails);
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "장바구니를 비웠습니다.", null));
    }
}
