package com.delivery.domain.payment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.delivery.domain.payment.dto.response.PaymentPageResponse;
import com.delivery.domain.payment.dto.response.PaymentResponse;
import com.delivery.domain.payment.entity.PaymentMethod;
import com.delivery.domain.payment.entity.PaymentStatus;
import com.delivery.domain.payment.service.PaymentService;
import com.delivery.global.exception.BusinessException;
import com.delivery.global.exception.ErrorCodeRegistry;
import com.delivery.global.exception.GlobalErrorCode;
import com.delivery.global.exception.GlobalExceptionHandler;
import com.delivery.global.security.config.CustomUserDetails;
import java.time.LocalDateTime;
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
class PaymentControllerUnitTest {

    @Mock private PaymentService paymentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.standaloneSetup(new PaymentController(paymentService))
                        .setControllerAdvice(new GlobalExceptionHandler(new ErrorCodeRegistry()))
                        .build();
    }

    @Test
    @DisplayName("존재하지 않는 결제 조회는 공통 실패 wrapper를 반환한다")
    void getPayment_fail_when_payment_does_not_exist() throws Exception {
        UUID paymentId = UUID.randomUUID();

        when(paymentService.getPayment(eq(paymentId), any(CustomUserDetails.class)))
                .thenThrow(new BusinessException(GlobalErrorCode.NOT_FOUND));

        mockMvc.perform(get("/api/v1/payments/{paymentId}", paymentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("잘못된 status enum 요청은 공통 실패 wrapper를 반환한다")
    void getMyPayments_fail_when_status_enum_is_invalid() throws Exception {
        mockMvc.perform(get("/api/v1/payments/me").param("status", "ABC"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.error").value("INVALID_PARAMETER_TYPE"));
    }

    @Test
    @DisplayName("결제 취소 요청 본문이 비어 있으면 공통 실패 wrapper를 반환한다")
    void cancelPayment_fail_when_cancel_reason_is_blank() throws Exception {
        UUID paymentId = UUID.randomUUID();

        mockMvc.perform(
                        patch("/api/v1/payments/{paymentId}/cancel", paymentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"cancelReason\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.error").value("REQUIRED_VALUE"));
    }

    @Test
    @DisplayName("결제 상세 조회 성공 시 공통 성공 wrapper를 반환한다")
    void getPayment_success_returns_wrapper() throws Exception {
        UUID paymentId = UUID.randomUUID();
        PaymentResponse response =
                new PaymentResponse(
                        paymentId,
                        UUID.randomUUID(),
                        1L,
                        PaymentStatus.PAID,
                        PaymentMethod.CARD,
                        15000,
                        LocalDateTime.now(),
                        null,
                        null);

        when(paymentService.getPayment(eq(paymentId), any(CustomUserDetails.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/{paymentId}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.paymentId").value(paymentId.toString()))
                .andExpect(jsonPath("$.error").value(Matchers.nullValue()));
    }

    @Test
    @DisplayName("가게 결제 목록 조회 성공 시 공통 성공 wrapper를 반환한다")
    void getStorePayments_success_returns_wrapper() throws Exception {
        UUID storeId = UUID.randomUUID();
        PaymentPageResponse response = new PaymentPageResponse(List.of(), 0, 10, 0, 0, false);

        when(paymentService.getStorePayments(
                        eq(storeId),
                        any(CustomUserDetails.class),
                        eq(0),
                        eq(10),
                        eq(PaymentStatus.PAID)))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/owner/stores/{storeId}/payments", storeId)
                                .param("page", "0")
                                .param("size", "10")
                                .param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.error").value(Matchers.nullValue()));
    }
}
