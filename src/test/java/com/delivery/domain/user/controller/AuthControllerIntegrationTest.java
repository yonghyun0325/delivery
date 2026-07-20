package com.delivery.domain.user.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.delivery.config.AbstractIntegrationTest;
import com.delivery.config.WithMockCustomUser;
import com.delivery.domain.user.dto.request.LoginRequest;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.exception.AuthErrorCode;
import com.delivery.domain.user.fixture.UserFixture;
import com.delivery.domain.user.repository.UserRepository;
import com.delivery.global.cache.RefreshTokenRepository;
import com.delivery.global.security.jwt.JwtUtil;
import com.delivery.global.security.principal.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerIntegrationTest extends AbstractIntegrationTest {
    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;

    @Autowired private RefreshTokenRepository refreshTokenRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired JwtUtil jwtUtil;

    private String username;
    private UUID sessionId;
    private String accessToken;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        User dummyUser = UserFixture.ROLE_CUSTOMER.createUserNoId("test1234", "dummy");
        dummyUser.updatedPassword(passwordEncoder.encode("Testtest123!"));

        User dummySavedUser = userRepository.save(dummyUser);
        username = dummySavedUser.getUsername();

        CustomUserDetails userDetails = CustomUserDetails.from(dummySavedUser);
        sessionId = UUID.randomUUID();
        accessToken =
                jwtUtil.generateAccessToken(userDetails, dummySavedUser.getUserUuid(), sessionId);
        refreshToken =
                jwtUtil.generateRefreshToken(userDetails, dummySavedUser.getUserUuid(), sessionId);

        refreshTokenRepository.save(sessionId, refreshToken);
    }

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 성공 시 Body로 Access Token, Cookie로 Refresh Token을 반환한다.")
    void signUp_success() throws Exception {
        // given
        var request = UserFixture.ROLE_CUSTOMER.createRequestDto();

        // when & then
        mockMvc.perform(
                        post("/api/v1/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true));

        var savedUser = userRepository.findByUsername(request.username());

        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getNickName()).isEqualTo(request.nickName());
    }

    @Test
    @DisplayName("로그인 성공 시 Body로 Access Token, Cookie로 Refresh Token을 반환한다.")
    void login_success() throws Exception {
        // given
        var request = new LoginRequest(username, "Testtest123!");

        // when & then
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class logout {
        @Test
        @WithMockCustomUser
        @DisplayName("로그아웃 성공 시 Access Token을 블랙리스트에 등록하고 Refresh Token을 쿠키와 캐시에서 지운다.")
        void logout_success() throws Exception {
            // when & then
            mockMvc.perform(
                            post("/api/v1/auth/logout")
                                    .with(csrf())
                                    .header("Authorization", "Bearer " + accessToken))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isEmpty());

            var savedRefreshToken = refreshTokenRepository.findByKey(sessionId);

            assertThat(savedRefreshToken).isNull();
        }

        @Test
        @DisplayName("AccessToken을 헤더에 담아 보내지 않으면 401(TOKEN_NOT_FOUND)를 반환한다.")
        void logout_fail_when_not_found_access_token() throws Exception {
            // when & then
            mockMvc.perform(post("/api/v1/auth/logout").with(csrf()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.error").value(AuthErrorCode.TOKEN_NOT_FOUND.name()))
                    .andExpect(
                            jsonPath("$.message")
                                    .value(AuthErrorCode.TOKEN_NOT_FOUND.getMessage()));
        }
    }

    @Nested
    @DisplayName("RefreshToken 재발급 테스트")
    class refresh {
        @Test
        @DisplayName(
                "재발급 성공 시 기존의 Refresh Token을 삭제하고 Body로 Access Token, Cookie로 Refresh Token을 반환한다.")
        void refresh_success() throws Exception {
            // given
            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);

            // when & then
            var response =
                    mockMvc.perform(
                                    post("/api/v1/auth/refresh")
                                            .with(csrf())
                                            .cookie(refreshTokenCookie)
                                            .contentType(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.success").value(true))
                            .andExpect(jsonPath("$.data").isNotEmpty())
                            .andExpect(cookie().exists("refreshToken"))
                            .andExpect(cookie().httpOnly("refreshToken", true))
                            .andReturn();
            ;

            Cookie newCookie = response.getResponse().getCookie("refreshToken");
            String newRefreshToken = newCookie.getValue();

            UUID newSessionId = jwtUtil.getSessionIdFromRefreshToken(newRefreshToken);

            String newSaveRefreshToken = refreshTokenRepository.findByKey(newSessionId);

            assertThat(newSaveRefreshToken).isNotNull();
            assertThat(newSaveRefreshToken).isEqualTo(newRefreshToken);
            assertThat(refreshTokenRepository.findByKey(sessionId)).isNull();
        }

        @Test
        @DisplayName("변조된 Refresh Token이면 401(INVALID_REFRESH_TOKEN) 에러를 반환한다.")
        void refresh_fail_when_invalid() throws Exception {
            // given
            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken + "123");

            // when & then
            mockMvc.perform(
                            post("/api/v1/auth/refresh")
                                    .with(csrf())
                                    .cookie(refreshTokenCookie)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(
                            jsonPath("$.error").value(AuthErrorCode.INVALID_REFRESH_TOKEN.name()))
                    .andExpect(
                            jsonPath("$.message")
                                    .value(AuthErrorCode.INVALID_REFRESH_TOKEN.getMessage()));
        }
    }
}
