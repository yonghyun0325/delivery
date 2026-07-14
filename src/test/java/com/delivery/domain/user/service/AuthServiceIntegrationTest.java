package com.delivery.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.delivery.config.AbstractIntegrationTest;
import com.delivery.domain.user.dto.request.LoginRequest;
import com.delivery.domain.user.dto.request.SignUpRequest;
import com.delivery.domain.user.dto.response.AuthResponse;
import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.entity.UserStatus;
import com.delivery.domain.user.repository.UserRepository;
import com.delivery.testutil.ConcurrencyTestingUtil;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceIntegrationTest extends AbstractIntegrationTest {
    @Autowired private ApplicationEventPublisher applicationEventPublisher;
    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

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
        assertThat(savedUser.getNickName()).isEqualTo(response.nickName());
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
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        ConcurrencyTestingUtil.run(
                threadCount,
                () -> {
                    try {
                        authService.signUp(request);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    }
                });

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(4);
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
        assertThat(loginResponse.nickName()).isEqualTo(savedUser.getNickName());
        assertThat(loginResponse.accessToken()).isNotBlank();
        assertThat(loginResponse.refreshToken()).isNotBlank();
    }
}
