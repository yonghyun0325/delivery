package com.delivery.domain.store.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.delivery.domain.store.dto.request.RegionRequest;
import com.delivery.domain.store.dto.response.RegionResponse;
import com.delivery.domain.store.service.RegionService;
import com.delivery.global.exception.ErrorCodeRegistry;
import com.delivery.global.security.config.CustomUserDetails;
import com.delivery.global.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
@WebMvcTest(RegionController.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RegionControllerUnitTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private RegionService regionService;
    @MockitoBean private ErrorCodeRegistry errorCodeRegistry;

    @BeforeEach
    void setUpSecurityContext() {
        CustomUserDetails mockUser = CustomUserDetails.builder()
                .id(1L)
                .username("admin")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_MANAGER")))
                .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("지역 등록 테스트")
    class CreateRegion {

        @Test
        @DisplayName("지역 등록 성공")
        void createRegion_success() throws Exception {
            UUID regionId = UUID.randomUUID();
            RegionRequest request = new RegionRequest("강남구", 37.5, 127.0);
            RegionResponse response = new RegionResponse(regionId, "강남구", 37.5, 127.0);

            given(regionService.createRegion(any(RegionRequest.class))).willReturn(response);

            mockMvc.perform(post("/api/v1/regions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.name").value("강남구"));

            verify(regionService).createRegion(any(RegionRequest.class));
        }

        @Test
        @DisplayName("이름이 빈 값이면 400 반환")
        void createRegion_fail_when_blank_name() throws Exception {
            RegionRequest request = new RegionRequest("", 37.5, 127.0);

            mockMvc.perform(post("/api/v1/regions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("지역 조회 테스트")
    class GetRegions {

        @Test
        @DisplayName("지역 목록 조회 성공")
        void getRegions_success() throws Exception {
            List<RegionResponse> response = List.of(
                    new RegionResponse(UUID.randomUUID(), "강남구", 37.5, 127.0),
                    new RegionResponse(UUID.randomUUID(), "서초구", 37.4, 127.0)
            );

            given(regionService.getRegions()).willReturn(response);

            mockMvc.perform(get("/api/v1/regions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2));

            verify(regionService).getRegions();
        }
    }

    @Nested
    @DisplayName("지역 수정 테스트")
    class UpdateRegion {

        @Test
        @DisplayName("지역 수정 성공")
        void updateRegion_success() throws Exception {
            UUID regionId = UUID.randomUUID();
            RegionRequest request = new RegionRequest("서초구", 37.4, 127.0);
            RegionResponse response = new RegionResponse(regionId, "서초구", 37.4, 127.0);

            given(regionService.updateRegion(eq(regionId), any(RegionRequest.class)))
                    .willReturn(response);

            mockMvc.perform(put("/api/v1/regions/{regionId}", regionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("서초구"));

            verify(regionService).updateRegion(eq(regionId), any(RegionRequest.class));
        }
    }

    @Nested
    @DisplayName("지역 삭제 테스트")
    class DeleteRegion {

        @Test
        @DisplayName("지역 삭제 성공")
        void deleteRegion_success() throws Exception {
            UUID regionId = UUID.randomUUID();

            mockMvc.perform(delete("/api/v1/regions/{regionId}", regionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("지역 삭제 성공"));

            verify(regionService).deleteRegion(eq(regionId), any());
        }
    }
}