package com.delivery.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.delivery.domain.auth.dto.AuthResponseDto;
import com.delivery.domain.auth.dto.LoginRequestDto;
import com.delivery.domain.auth.dto.SignUpRequestDto;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.enums.Role;
import com.delivery.domain.user.enums.UserStatus;
import com.delivery.domain.user.repository.UserRepository;
import com.delivery.testconfig.AbstractIntegrationTest;
import com.delivery.testutil.ConcurrencyTestingUtil;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class AuthServiceIntegrationTest extends AbstractIntegrationTest {
    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    @Transactional
    @DisplayName("회원가입 성공")
    void signUp_success() {
        // given
        SignUpRequestDto request =
                SignUpRequestDto.builder()
                        .username("test1234")
                        .password("testtest1234!")
                        .nickName("test")
                        .phoneNumber("01012345678")
                        .role(Role.CUSTOMER)
                        .build();

        // when
        AuthResponseDto response = authService.signUp(request);

        // then
        User savedUser =
                userRepository
                        .findWithRolesByUsernameAndDeletedAtIsNull(request.getUsername())
                        .orElseThrow();

        assertThat(savedUser.getId()).isEqualTo(response.getId());
        assertThat(savedUser.getUsername()).isEqualTo(response.getUsername());
        assertThat(passwordEncoder.matches("testtest1234!", savedUser.getPassword())).isTrue();
        assertThat(savedUser.getNickName()).isEqualTo(response.getNickName());
        assertThat(response.getAccessToken()).isNotBlank();
    }

    @Test
    @DisplayName(("회원가입 시 동시 클릭하는 경우 하나만 성공해야 한다."))
    void signUp_fail_when_duplicate_on_concurrency() throws InterruptedException {
        // given
        SignUpRequestDto request =
                SignUpRequestDto.builder()
                        .username("test123456")
                        .password("testtest1234!")
                        .nickName("test12345")
                        .phoneNumber("01012345678")
                        .role(Role.CUSTOMER)
                        .build();
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
        LoginRequestDto loginRequest =
                LoginRequestDto.builder().username("test1234").password("testtest1234!").build();

        // when
        AuthResponseDto loginResponse = authService.login(loginRequest);

        // then
        assertThat(loginResponse.getId()).isEqualTo(savedUser.getId());
        assertThat(loginResponse.getUsername()).isEqualTo(savedUser.getUsername());
        assertThat(loginResponse.getNickName()).isEqualTo(savedUser.getNickName());
        assertThat(loginResponse.getAccessToken()).isNotBlank();
    }
}
