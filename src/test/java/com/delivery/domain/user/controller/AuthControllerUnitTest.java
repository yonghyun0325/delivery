package com.delivery.domain.user.controller;

import static com.delivery.global.security.jwt.JwtHeaderType.REFRESH_TOKEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.delivery.config.AbstractControllerTest;
import com.delivery.config.WithMockCustomUser;
import com.delivery.domain.user.dto.request.LoginRequest;
import com.delivery.domain.user.dto.request.SignUpRequest;
import com.delivery.domain.user.dto.response.AuthResponse;
import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.exception.AuthErrorCode;
import com.delivery.domain.user.exception.AuthException;
import com.delivery.domain.user.service.AuthService;
import java.util.stream.Stream;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerUnitTest extends AbstractControllerTest {
    @MockitoBean private AuthService authService;

    @Nested
    @DisplayName("회원가입 테스트")
    class SignUp {
        @Test
        @DisplayName("회원가입 성공")
        void signUp_success() throws Exception {
            // given
            SignUpRequest request =
                    new SignUpRequest(
                            "test1234", "Testtest123!", "test", "01012345678", Role.CUSTOMER);

            AuthResponse response = new AuthResponse("test1234", "accessToken", "refreshToken");

            given(authService.signUp((any(SignUpRequest.class)))).willReturn(response);

            // when & then
            mockMvc.perform(
                            post("/api/v1/auth")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(cookie().value("refreshToken", "refreshToken"))
                    .andExpect(cookie().httpOnly("refreshToken", true))
                    .andExpect(cookie().secure("refreshToken", false))
                    .andExpect(jsonPath("$.data").value("accessToken"));

            verify(authService).signUp(any(SignUpRequest.class));
        }

        @DisplayName("회원가입 시 유효성 체크를 통과하지 못하면 예외가 발생해야한다.")
        @ParameterizedTest
        @MethodSource("testCase")
        void signUp_fail_when_invalid(SignUpRequest request) throws Exception {
            // when & then
            mockMvc.perform(
                            post("/api/v1/auth")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(authService);
        }

        static Stream<SignUpRequest> testCase() {
            return Stream.of(
                    // 아이디 유효성 검사 실패
                    new SignUpRequest("아이디", "Testtest123!", "test", "01012345678", Role.CUSTOMER),

                    // 비밀번호 유효성 검사 실패
                    new SignUpRequest("test1234", "비밀번호", "test", "01012345678", Role.CUSTOMER),

                    // 닉네임 유효성 검사 실패
                    new SignUpRequest(
                            "test1234",
                            "Testtest123!",
                            "테스트테스트테스트테스트테스트테스트",
                            "01012345678",
                            Role.CUSTOMER),

                    // 전화번호 유효성 검사 실패
                    new SignUpRequest("test1234", "Testtest123!", "test", "연락처", Role.CUSTOMER));
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class login {
        @Test
        @DisplayName("로그인 성공")
        void login_success() throws Exception {
            // given
            LoginRequest request = new LoginRequest("test1234", "Testtest123!");

            AuthResponse response = new AuthResponse("test1234", "accessToken", "refreshToken");

            given(authService.login(any(LoginRequest.class))).willReturn(response);

            // when & then
            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(cookie().value("refreshToken", "refreshToken"))
                    .andExpect(cookie().httpOnly("refreshToken", true))
                    .andExpect(cookie().secure("refreshToken", false))
                    .andExpect(jsonPath("$.data").value("accessToken"));

            verify(authService).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("로그인 시 아이디 또는 비밀번호 입력을 안하면 예외가 발생해야한다.")
        void login_fail_when_invalid() throws Exception {
            // given
            LoginRequest request = new LoginRequest("", "Testtext123!");

            LoginRequest request2 = new LoginRequest("test1234", "");

            // when & then
            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("아이디를 입력해주세요."));
            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request2)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("비밀번호를 입력해주세요."));

            verifyNoInteractions(authService);
        }
    }

    @Nested
    @DisplayName("Refresh Token")
    class refresh {
        @Test
        @WithMockCustomUser
        @DisplayName("리프래시 토큰 발급에 성공한다.")
        void refresh_success() throws Exception {
            // given
            String refreshToken = "refreshToken";
            AuthResponse response = new AuthResponse("test1234", "token", refreshToken);
            given(authService.refresh(any())).willReturn(response);

            // when & then
            mockMvc.perform(
                            post("/api/v1/auth/refresh")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .cookie(new Cookie("refreshToken", "expired-token-value")))
                    .andExpect(status().isOk())
                    .andExpect(cookie().value("refreshToken", refreshToken))
                    .andExpect(cookie().httpOnly("refreshToken", true))
                    .andExpect(cookie().secure("refreshToken", false))
                    .andExpect(jsonPath("$.data").value("token"));
        }

        @Test
        @DisplayName("만료된 리프래시 토큰인 경우 EXPIRED_REFRESH_TOKEN 예외를 반환한다.")
        void refresh_fail_when_expired_token() throws Exception {
            // given
            given(authService.refresh(any()))
                    .willThrow(new AuthException(AuthErrorCode.EXPIRED_REFRESH_TOKEN));

            // when & then
            mockMvc.perform(post("/api/v1/auth/refresh")
                    .cookie(new Cookie("refreshToken", "expired-token-value")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(
                            jsonPath("$.message")
                                    .value(AuthErrorCode.EXPIRED_REFRESH_TOKEN.getMessage()));
        }
    }
}
