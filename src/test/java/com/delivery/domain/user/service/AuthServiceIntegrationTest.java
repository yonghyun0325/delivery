package com.delivery.domain.user.service;

import com.delivery.config.AbstractIntegrationTest;
import com.delivery.config.WithMockCustomUser;
import com.delivery.domain.user.dto.request.LoginRequest;
import com.delivery.domain.user.dto.request.SignUpRequest;
import com.delivery.domain.user.dto.response.AuthResponse;
import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.entity.UserStatus;
import com.delivery.domain.user.exception.AuthErrorCode;
import com.delivery.domain.user.exception.AuthException;
import com.delivery.domain.user.exception.UserErrorCode;
import com.delivery.domain.user.exception.UserException;
import com.delivery.domain.user.fixture.UserFixture;
import com.delivery.domain.user.repository.UserRepository;
import com.delivery.global.cache.RefreshTokenRepository;
import com.delivery.global.security.config.CustomUserDetails;
import com.delivery.global.security.jwt.JwtUtil;
import com.delivery.testutil.ConcurrencyTestingUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static com.delivery.global.security.jwt.JwtHeaderType.REFRESH_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceIntegrationTest extends AbstractIntegrationTest {
    @Autowired private ApplicationEventPublisher applicationEventPublisher;
    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private JwtUtil jwtUtil;

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
    }

    @Test
    @Transactional
    @DisplayName("회원가입 성공")
    void signUp_success() {
        // given
        SignUpRequest request =
                new SignUpRequest(
                        "test1234", "testtest1234!", "test", "01012345678", Role.CUSTOMER);

        // when
        AuthResponse response = authService.signUp(request);

        User savedUser =
                userRepository
                        .findWithRolesByUsernameAndDeletedAtIsNull(request.username())
                        .orElseThrow();

        // then
        assertThat(savedUser.getUsername()).isEqualTo(response.username());
        assertThat(passwordEncoder.matches("testtest1234!", savedUser.getPassword())).isTrue();
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
    }

    @Test
    @DisplayName(("회원가입 시 동시 클릭하는 경우 하나만 성공해야 한다."))
    void signUp_fail_when_duplicate_on_concurrency() throws InterruptedException {
        // given
        SignUpRequest request =
                new SignUpRequest(
                        "test123456", "testtest1234!", "test12345", "01012345678", Role.CUSTOMER);
        int threadCount = 5;

        AtomicInteger successCount = new AtomicInteger(0);

        // when
        List<Exception> failures = new CopyOnWriteArrayList<>();
        ConcurrencyTestingUtil.run(threadCount, () -> {
            try {
                authService.signUp(request);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failures.add(e);
            }
        });

            assertThat(failures).hasSize(4);
            assertThat(failures).allSatisfy(e -> {
            assertThat(e).isInstanceOf(UserException.class);
            assertThat(((UserException) e).getErrorCode()).isEqualTo(UserErrorCode.DUPLICATE_USERNAME);
        });
    }

    @Test
    @Transactional
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        Set<Role> roles = new HashSet<>();
        roles.add(Role.CUSTOMER);

        User user =
                User.builder()
                        .username("test1234")
                        .password(passwordEncoder.encode("testtest1234!"))
                        .nickName("test")
                        .phoneNumber("01012345678")
                        .userStatus(UserStatus.ACTIVE)
                        .roles(roles)
                        .build();

        User savedUser = userRepository.save(user);
        LoginRequest loginRequest = new LoginRequest("test1234", "testtest1234!");

        // when
        AuthResponse loginResponse = authService.login(loginRequest);

        // then
        assertThat(loginResponse.username()).isEqualTo(savedUser.getUsername());
        assertThat(loginResponse.accessToken()).isNotBlank();
        assertThat(loginResponse.refreshToken()).isNotBlank();
    }

    @Nested
    @DisplayName("리프래시 토큰 발급")
    class refresh {
        @Test
        @DisplayName("리프래시 토큰 발급에 성공하면 액세스 토큰과 리프래시 토큰을 새로 발급한다.")
        void refresh_success() {
            // given
            var user = userRepository.save(UserFixture.ROLE_CUSTOMER.createUserNoId());
            CustomUserDetails userDetails = CustomUserDetails.from(user);

            UUID userUuid = userDetails.getUserUuid();
            UUID sessionId = UUID.randomUUID();

            String refreshToken = jwtUtil.generateRefreshToken(userDetails, userUuid, sessionId);
            refreshTokenRepository.save(sessionId, refreshToken);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(REFRESH_TOKEN.getHeader(), refreshToken);

            var savedToken = refreshTokenRepository.findByKey(sessionId);

            // when

            AuthResponse actual = authService.refresh(jwtUtil.resolveRefreshToken(request));

            // then
            assertThat(actual.accessToken()).isNotNull();
            assertThat(actual.refreshToken()).isNotNull();

            assertThat(savedToken).isNotBlank();
        }

        @Test
        @DisplayName("탈퇴하거나 존재하지 않는 회원이 리프래시 토큰 반환을 할 경우 404 (NOT_EXIST_USER) 예외를 반환한다.")
        void refresh_fail_when_not_exist_user() {
            // given
            var user = userRepository.save(UserFixture.ROLE_CUSTOMER.createUserNoId());
            CustomUserDetails userDetails = CustomUserDetails.from(user);

            UUID userUuid = userDetails.getUserUuid();
            UUID sessionId = UUID.randomUUID();

            String refreshToken = jwtUtil.generateRefreshToken(userDetails, userUuid, sessionId);
            refreshTokenRepository.save(sessionId, refreshToken);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(REFRESH_TOKEN.getHeader(), refreshToken);

            userRepository.delete(user);

            // when & then
            assertThatThrownBy(() -> authService.refresh(jwtUtil.resolveRefreshToken(request)))
                    .isInstanceOf(UserException.class)
                    .hasMessage(UserErrorCode.NOT_EXIST_USER.getMessage());
        }

        @Test
        @DisplayName("리프래시 토큰이 공백이면 INVALID_REFRESH_TOKEN 예외를 반환한다.")
        void refresh_fail_when_invalid_refresh_token() {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(REFRESH_TOKEN.getHeader(), " ");

            // when & then
            assertThatThrownBy(() -> authService.refresh(jwtUtil.resolveRefreshToken(request)))
                    .isInstanceOf(AuthException.class)
                    .hasMessage(AuthErrorCode.INVALID_REFRESH_TOKEN.getMessage());
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class logout {
        @Test
        @WithMockCustomUser
        @DisplayName("로그아웃 시 리프래시 토큰이 삭제되어야 한다.")
        void logout_with_delete_refresh_token() {
            // given
            UUID userUuid = UUID.randomUUID();
            UUID sessionId = UUID.randomUUID();

            User user = UserFixture.ROLE_CUSTOMER.createUser(1L);
            CustomUserDetails userDetails = CustomUserDetails.from(user);

            // when
            refreshTokenRepository.save(sessionId, "refreshToken");

            String accessToken = jwtUtil.generateAccessToken(userDetails, userUuid, sessionId);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + accessToken);

            authService.logout(request);

            String token = refreshTokenRepository.findByKey(sessionId);

            // then
            assertThat(token).isNull();
        }

        @Test
        @DisplayName("로그아웃 시 액세스 토큰이 없으면 INVALID_REFRESH_TOKEN 예외를 반환한다.")
        void logout_with_access_token_is_null() {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "");

            // when & then
            assertThatThrownBy(() -> authService.logout(request))
                    .isInstanceOf(AuthException.class)
                    .hasMessage(AuthErrorCode.INVALID_ACCESS_TOKEN.getMessage());
        }
    }
}
