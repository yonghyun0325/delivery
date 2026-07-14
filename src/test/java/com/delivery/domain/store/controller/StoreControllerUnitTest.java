package com.delivery.domain.store.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.delivery.domain.store.dto.request.StoreRequest;
import com.delivery.domain.store.dto.response.StoreResponse;
import com.delivery.domain.store.service.StoreService;
import com.delivery.global.cache.RefreshTokenRepository;
import com.delivery.global.exception.ErrorCodeRegistry;
import com.delivery.global.security.config.CustomUserDetails;
import com.delivery.global.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import static org.mockito.ArgumentMatchers.anyBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@RequiredArgsConstructor
@WebMvcTest(StoreController.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class StoreControllerUnitTest {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockitoBean private RefreshTokenRepository refreshTokenRepository;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private StoreService storeService;
    @MockitoBean private ErrorCodeRegistry errorCodeRegistry;

    @BeforeEach
    void setUpSecurityContext() {
        CustomUserDetails mockUser = CustomUserDetails.builder()
                .id(1L)
                .username("testuser")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_OWNER")))
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private StoreRequest createStoreRequest() {
        return new StoreRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "테스트 가게",
                "서울시 강남구",
                "01012345678",
                "테스트 가게입니다",
                10000);
    }

    private StoreResponse createStoreResponse(UUID storeId) {
        return new StoreResponse(
                storeId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "테스트 가게",
                "서울시 강남구",
                "01012345678",
                "테스트 가게입니다",
                10000,
                false,
                0.0,
                null,
                null);
    }

    @Nested
    @DisplayName("가게 등록 테스트")
    class CreateStore {
        @Test
        @DisplayName("가게 등록 성공")
        void createStore_success() throws Exception {
            // given
            UUID storeId = UUID.randomUUID();
            StoreRequest request = createStoreRequest();
            StoreResponse response = createStoreResponse(storeId);

            given(storeService.createStore(any(), any(StoreRequest.class))).willReturn(response);

            // when & then
            mockMvc.perform(
                            post("/api/v1/stores")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.name").value("테스트 가게"))
                    .andExpect(jsonPath("$.data.address").value("서울시 강남구"));

            verify(storeService).createStore(any(), any(StoreRequest.class));
        }

        @Test
        @DisplayName("가게 등록 시 이름이 빈 값이면 400 반환")
        void createStore_fail_when_invalid() throws Exception {
            // given
            StoreRequest request = new StoreRequest(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "",
                    "서울시 강남구",
                    "01012345678",
                    "테스트",
                    10000);

            // when & then
            mockMvc.perform(
                            post("/api/v1/stores")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("가게 조회 테스트")
    class GetStore {
        @Test
        @DisplayName("가게 단건 조회 성공")
        void getStore_success() throws Exception {
            // given
            UUID storeId = UUID.randomUUID();
            StoreResponse response = createStoreResponse(storeId);

            given(storeService.getStore(eq(storeId))).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/v1/stores/{storeId}", storeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("테스트 가게"));

            verify(storeService).getStore(eq(storeId));
        }
    }

    @Nested
    @DisplayName("가게 삭제 테스트")
    class DeleteStore {
        @Test
        @DisplayName("가게 삭제 성공")
        void deleteStore_success() throws Exception {
            // given
            UUID storeId = UUID.randomUUID();

            // when & then
            mockMvc.perform(delete("/api/v1/stores/{storeId}", storeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("가게 삭제 성공"));

            verify(storeService).deleteStore(eq(storeId), any(), anyBoolean(), any());
        }
    }
}