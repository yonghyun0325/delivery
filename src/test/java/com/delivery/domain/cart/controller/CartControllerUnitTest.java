package com.delivery.domain.cart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.delivery.domain.cart.dto.response.CartItemResponse;
import com.delivery.domain.cart.dto.response.CartResponse;
import com.delivery.domain.cart.service.CartService;
import com.delivery.global.exception.BusinessException;
import com.delivery.global.exception.ErrorCodeRegistry;
import com.delivery.global.exception.GlobalErrorCode;
import com.delivery.global.exception.GlobalExceptionHandler;
import com.delivery.global.security.config.CustomUserDetails;
import java.util.List;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CartControllerUnitTest {

    @Mock private CartService cartService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.standaloneSetup(new CartController(cartService))
                        .setControllerAdvice(new GlobalExceptionHandler(new ErrorCodeRegistry()))
                        .build();
    }

    @Test
    @DisplayName("returns common wrapper for my cart response")
    void getMyCart_success_returns_wrapper() throws Exception {
        CartItemResponse item =
                new CartItemResponse(UUID.randomUUID(), UUID.randomUUID(), "Kimchi", 2, 12000L, 24000L);
        CartResponse response =
                new CartResponse(UUID.randomUUID(), 1L, UUID.randomUUID(), null, List.of(item), 2, 24000L);

        when(cartService.getMyCart(any(CustomUserDetails.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/carts/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.totalPrice").value(24000))
                .andExpect(jsonPath("$.data.items[0].menuName").value("Kimchi"))
                .andExpect(jsonPath("$.error").value(Matchers.nullValue()));
    }

    @Test
    @DisplayName("returns common fail wrapper when add quantity is invalid")
    void addCartItem_fail_when_quantity_is_invalid() throws Exception {
        mockMvc.perform(
                        post("/api/v1/carts/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"menuId\":\"" + UUID.randomUUID() + "\",\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("returns common wrapper for cart item update")
    void updateCartItem_success_returns_wrapper() throws Exception {
        UUID cartItemId = UUID.randomUUID();
        CartResponse response = new CartResponse(null, 1L, null, null, List.of(), 3, 21000L);

        when(cartService.updateCartItem(isNull(), eq(cartItemId), eq(3))).thenReturn(response);

        mockMvc.perform(
                        patch("/api/v1/carts/items/{cartItemId}", cartItemId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalQuantity").value(3))
                .andExpect(jsonPath("$.data.totalPrice").value(21000))
                .andExpect(jsonPath("$.error").value(Matchers.nullValue()));
    }

    @Test
    @DisplayName("returns common fail wrapper when update quantity is invalid")
    void updateCartItem_fail_when_quantity_is_invalid() throws Exception {
        UUID cartItemId = UUID.randomUUID();

        mockMvc.perform(
                        patch("/api/v1/carts/items/{cartItemId}", cartItemId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("returns common fail wrapper for forbidden delete")
    void deleteCartItem_fail_when_forbidden() throws Exception {
        UUID cartItemId = UUID.randomUUID();

        doThrow(new BusinessException(GlobalErrorCode.FORBIDDEN))
                .when(cartService)
                .deleteCartItem(isNull(), eq(cartItemId));

        mockMvc.perform(delete("/api/v1/carts/items/{cartItemId}", cartItemId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("returns common wrapper for clear cart")
    void clearMyCart_success_returns_wrapper() throws Exception {
        mockMvc.perform(delete("/api/v1/carts/me/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.error").value(Matchers.nullValue()));
    }
}
