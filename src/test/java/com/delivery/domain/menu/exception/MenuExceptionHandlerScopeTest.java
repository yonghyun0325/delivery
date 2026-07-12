package com.delivery.domain.menu.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.delivery.domain.auth.controller.AuthController;
import com.delivery.domain.auth.dto.request.SignUpRequest;
import com.delivery.domain.auth.service.AuthService;
import com.delivery.domain.menu.controller.MenuController;
import com.delivery.domain.menu.dto.request.CreateMenuRequest;
import com.delivery.domain.menu.service.MenuService;
import com.delivery.domain.user.enums.Role;
import com.delivery.global.security.jwt.JwtRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// MenuExceptionHandler(assignableTypes = MenuController.class)가 정말로 MenuController에만
// 적용되고 다른 컨트롤러(AuthController)의 예외 처리는 그대로 GlobalExceptionHandler가
// 담당하는지 확인하는 테스트. 두 컨트롤러를 한 슬라이스에 같이 올려야 스코프 누수 여부를
// 검증할 수 있어서 별도 파일로 분리함.
@WebMvcTest(controllers = {MenuController.class, AuthController.class})
@AutoConfigureMockMvc(addFilters = false)
class MenuExceptionHandlerScopeTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private MenuService menuService;

    @MockitoBean private AuthService authService;

    @MockitoBean private JwtRequestFilter jwtRequestFilter;

    @Nested
    @DisplayName("MenuExceptionHandler 적용 범위")
    class Scope {

        @Test
        @DisplayName("MenuController의 검증 실패는 MenuExceptionHandler가 처리한다")
        void menuValidationFailure_usesMenuErrorCode() throws Exception {
            UUID storeId = UUID.randomUUID();
            CreateMenuRequest request = new CreateMenuRequest("", "설명", 8000, false, null);

            mockMvc.perform(
                            post("/api/v1/stores/{storeId}/menus", storeId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("INVALID_MENU_NAME"));
        }

        @Test
        @DisplayName("AuthController의 검증 실패는 MenuExceptionHandler의 영향을 받지 않고 그대로 처리된다")
        void authValidationFailure_notAffectedByMenuHandler() throws Exception {
            SignUpRequest request =
                    SignUpRequest.builder()
                            .username("아이디") // 패턴 위반(소문자/숫자만 허용)
                            .password("Testtest123!")
                            .nickName("test")
                            .phoneNumber("01012345678")
                            .role(Role.CUSTOMER)
                            .build();

            mockMvc.perform(
                            post("/api/v1/auth")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("INVALID_USERNAME"));
        }
    }
}
