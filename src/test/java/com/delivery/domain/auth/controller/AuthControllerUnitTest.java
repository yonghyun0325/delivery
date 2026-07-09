package com.delivery.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.delivery.domain.auth.dto.AuthResponseDto;
import com.delivery.domain.auth.dto.LoginRequestDto;
import com.delivery.domain.auth.dto.SignUpRequestDto;
import com.delivery.domain.auth.service.AuthService;
import com.delivery.domain.user.enums.Role;
import com.delivery.global.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@RequiredArgsConstructor
@WebMvcTest(AuthController.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AuthControllerUnitTest {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private AuthService authService;

    @Nested
    @DisplayName("회원가입 테스트")
    class SignUp {
        @Test
        @DisplayName("회원가입 성공")
        void signUp_success() throws Exception {
            // given
            SignUpRequestDto request =
                    SignUpRequestDto.builder()
                            .username("test1234")
                            .password("Testtest123!")
                            .nickName("test")
                            .phoneNumber("01012345678")
                            .role(Role.CUSTOMER)
                            .build();

            AuthResponseDto response =
                    AuthResponseDto.builder()
                            .id(1L)
                            .username("test1234")
                            .nickName("test")
                            .accessToken("accessToken")
                            .build();

            given(authService.signUp((any(SignUpRequestDto.class)))).willReturn(response);

            // when & then
            mockMvc.perform(
                            post("/api/v1/auth")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.username").value("test1234"))
                    .andExpect(jsonPath("$.data.nickName").value("test"))
                    .andExpect(jsonPath("$.data.accessToken").value("accessToken"));

            verify(authService).signUp(any(SignUpRequestDto.class));
        }

        @DisplayName("회원가입 시 유효성 체크를 통과하지 못하면 예외가 발생해야한다.")
        @ParameterizedTest
        @MethodSource("testCase")
        void signUp_fail_when_invalid(SignUpRequestDto request) throws Exception {
            // when & then
            mockMvc.perform(
                            post("/api/v1/auth")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        static Stream<SignUpRequestDto> testCase() {
            return Stream.of(
                    // 아이디 유효성 검사 실패
                    SignUpRequestDto.builder()
                            .username("아이디")
                            .password("Testtest123!")
                            .nickName("test")
                            .phoneNumber("01012345678")
                            .role(Role.CUSTOMER)
                            .build(),

                    // 비밀번호 유효성 검사 실패
                    SignUpRequestDto.builder()
                            .username("test1234")
                            .password("비밀번호")
                            .nickName("test")
                            .phoneNumber("01012345678")
                            .role(Role.CUSTOMER)
                            .build(),

                    // 닉네임 유효성 검사 실패
                    SignUpRequestDto.builder()
                            .username("test1234")
                            .password("Testtest123!")
                            .nickName("테스트테스트테스트테스트테스트테스트")
                            .phoneNumber("01012345678")
                            .role(Role.CUSTOMER)
                            .build(),

                    // 전화번호 유효성 검사 실패
                    SignUpRequestDto.builder()
                            .username("test1234")
                            .password("Testtest123!")
                            .nickName("test")
                            .phoneNumber("연락처")
                            .role(Role.CUSTOMER)
                            .build());
        }

        @Nested
        @DisplayName("로그인 테스트")
        class login {
            @Test
            @DisplayName("로그인 성공")
            void login_success() throws Exception {
                // given
                LoginRequestDto request =
                        LoginRequestDto.builder()
                                .username("test1234")
                                .password("Testtest123!")
                                .build();

                AuthResponseDto response =
                        AuthResponseDto.builder()
                                .id(1L)
                                .username("test1234")
                                .nickName("test")
                                .accessToken("accessToken")
                                .build();

                given(authService.login(any(LoginRequestDto.class))).willReturn(response);

                // when & then
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.data.id").value(1L))
                        .andExpect(jsonPath("$.data.username").value("test1234"))
                        .andExpect(jsonPath("$.data.nickName").value("test"))
                        .andExpect(jsonPath("$.data.accessToken").value("accessToken"));

                verify(authService).login(any(LoginRequestDto.class));
            }

            @Test
            @DisplayName("로그인 시 아이디 또는 비밀번호 입력을 안하면 예외가 발생해야한다.")
            void login_fail_when_invalid() throws Exception {
                // given
                LoginRequestDto request =
                        LoginRequestDto.builder().username("").password("Testtext123!").build();

                LoginRequestDto request2 =
                        LoginRequestDto.builder().username("test1234").password("").build();

                // when & then
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.message").value("아이디가 존재하지 않거나 비밀번호가 올바르지 않습니다."));
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request2)))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.message").value("아이디가 존재하지 않거나 비밀번호가 올바르지 않습니다."));
            }
        }
    }
}
