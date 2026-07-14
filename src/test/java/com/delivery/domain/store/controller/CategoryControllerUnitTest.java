package com.delivery.domain.store.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.delivery.domain.store.dto.request.CategoryRequest;
import com.delivery.domain.store.dto.response.CategoryResponse;
import com.delivery.domain.store.service.CategoryService;
import com.delivery.global.cache.RefreshTokenRepository;
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
@WebMvcTest(CategoryController.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class CategoryControllerUnitTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockitoBean private RefreshTokenRepository refreshTokenRepository;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private CategoryService categoryService;
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
    @DisplayName("카테고리 등록 테스트")
    class CreateCategory {

        @Test
        @DisplayName("카테고리 등록 성공")
        void createCategory_success() throws Exception {
            UUID categoryId = UUID.randomUUID();
            CategoryRequest request = new CategoryRequest("한식");
            CategoryResponse response = new CategoryResponse(categoryId, "한식");

            given(categoryService.createCategory(any(CategoryRequest.class))).willReturn(response);

            mockMvc.perform(post("/api/v1/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.name").value("한식"));

            verify(categoryService).createCategory(any(CategoryRequest.class));
        }

        @Test
        @DisplayName("이름이 빈 값이면 400 반환")
        void createCategory_fail_when_blank_name() throws Exception {
            CategoryRequest request = new CategoryRequest("");

            mockMvc.perform(post("/api/v1/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("카테고리 조회 테스트")
    class GetCategories {

        @Test
        @DisplayName("카테고리 목록 조회 성공")
        void getCategories_success() throws Exception {
            List<CategoryResponse> response = List.of(
                    new CategoryResponse(UUID.randomUUID(), "한식"),
                    new CategoryResponse(UUID.randomUUID(), "중식")
            );

            given(categoryService.getCategories()).willReturn(response);

            mockMvc.perform(get("/api/v1/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2));

            verify(categoryService).getCategories();
        }
    }

    @Nested
    @DisplayName("카테고리 수정 테스트")
    class UpdateCategory {

        @Test
        @DisplayName("카테고리 수정 성공")
        void updateCategory_success() throws Exception {
            UUID categoryId = UUID.randomUUID();
            CategoryRequest request = new CategoryRequest("중식");
            CategoryResponse response = new CategoryResponse(categoryId, "중식");

            given(categoryService.updateCategory(eq(categoryId), any(CategoryRequest.class)))
                    .willReturn(response);

            mockMvc.perform(put("/api/v1/categories/{categoryId}", categoryId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("중식"));

            verify(categoryService).updateCategory(eq(categoryId), any(CategoryRequest.class));
        }
    }

    @Nested
    @DisplayName("카테고리 삭제 테스트")
    class DeleteCategory {

        @Test
        @DisplayName("카테고리 삭제 성공")
        void deleteCategory_success() throws Exception {
            UUID categoryId = UUID.randomUUID();

            mockMvc.perform(delete("/api/v1/categories/{categoryId}", categoryId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("카테고리 삭제 성공"));

            verify(categoryService).deleteCategory(eq(categoryId), any());
        }
    }
}