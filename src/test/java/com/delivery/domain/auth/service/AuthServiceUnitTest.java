package com.delivery.domain.auth.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.delivery.domain.auth.dto.LoginRequestDto;
import com.delivery.domain.auth.dto.SignUpRequestDto;
import com.delivery.domain.auth.exception.AuthException;
import com.delivery.domain.user.enums.Role;
import com.delivery.domain.user.exception.UserException;
import com.delivery.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @InjectMocks private AuthService authService;

    @Nested
    @DisplayName("회원가입 실패 테스트")
    class SignUp {
        @Test
        @DisplayName("이미 존재하는 사용자일 시 예외가 발생해야한다.")
        void signUp_fail_when_username_is_duplicate() {
            // given
            SignUpRequestDto request =
                    SignUpRequestDto.builder()
                            .username("test1234")
                            .password("testtest1234!")
                            .nickName("test")
                            .phoneNumber("01012345678")
                            .role(Role.CUSTOMER)
                            .build();

            when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signUp(request))
                    .isInstanceOf(UserException.class)
                    .hasMessage("이미 사용중인 아이디입니다.");

            verify(userRepository).existsByUsername(request.getUsername());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("이미 존재하는 닉네임일 시 예외가 발생해야한다.")
        void signUp_fail_when_nickname_is_duplicate() {
            // given
            SignUpRequestDto request =
                    SignUpRequestDto.builder()
                            .username("test1234")
                            .password("testtest1234!")
                            .nickName("test")
                            .phoneNumber("01012345678")
                            .role(Role.CUSTOMER)
                            .build();

            when(userRepository.existsByNickName(request.getNickName())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signUp(request))
                    .isInstanceOf(UserException.class)
                    .hasMessage("이미 사용중인 닉네임입니다.");

            verify(userRepository).existsByNickName(request.getNickName());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("로그인 실패 테스트")
    class Login {
        @Test
        @DisplayName("로그인 시 존재하지 않는 아이디를 입력하면 예외가 발생해야한다.")
        void login_fail_when_invalid_login() {
            // given
            LoginRequestDto request =
                    LoginRequestDto.builder()
                            .username("test1234")
                            .password("testtest1234!")
                            .build();

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new InternalAuthenticationServiceException("아이디"));

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(AuthException.class)
                    .hasMessage("아이디가 존재하지 않거나 비밀번호가 올바르지 않습니다.");

            verify(authenticationManager)
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("로그인 시 틀린 비밀번호를 입력하면 예외가 발생해야한다.")
        void login_fail_when_invalid_password() {
            // given
            LoginRequestDto request =
                    LoginRequestDto.builder()
                            .username("test1234")
                            .password("testtest1234!")
                            .build();

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("비밀번호"));

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(AuthException.class)
                    .hasMessage("아이디가 존재하지 않거나 비밀번호가 올바르지 않습니다.");

            verify(authenticationManager)
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));
        }
    }
}
