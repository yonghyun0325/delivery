package com.delivery.domain.user.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import com.delivery.domain.user.dto.request.UpdateUserRoleRequest;
import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.exception.UserErrorCode;
import com.delivery.domain.user.exception.UserException;
import com.delivery.domain.user.fixture.UserFixture;
import com.delivery.domain.user.repository.UserRepository;
import java.util.Optional;

import com.delivery.global.cache.UserCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceUnitTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserCacheRepository userCacheRepository;
    @InjectMocks private UserAdminService userAdminService;

    private User user;

    @BeforeEach
    void setUp() {
        user = UserFixture.ROLE_CUSTOMER.createUser(1L);
    }

    @Nested
    @DisplayName("회원 단건 조회(관리자)")
    class findUserInfo {
        @Test
        @DisplayName("회원 단건 조회 성공")
        void findUserInfo_success() {
            // given
            Long userId = 1L;
            when(userRepository.findWithRolesById(eq(userId))).thenReturn(Optional.of(user));

            // when
            var result = userAdminService.findUserInfo(userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo(user.getUsername());
        }

        @Test
        @DisplayName("회원 단건 조회 실패 - 값이 없는 경우")
        void findUserInfo_fail_not_found() {
            // given
            Long userId = 1L;
            when(userRepository.findWithRolesById(eq(userId))).thenReturn(Optional.empty());

            // when & then
            UpdateUserRoleRequest request = new UpdateUserRoleRequest(Role.OWNER); // 테스트용 객체 생성
            assertThatThrownBy(() -> userAdminService.updateUserRole(userId, request))
                    .isInstanceOf(UserException.class)
                    .hasMessage(UserErrorCode.NOT_EXIST_USER.getMessage());

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("회원 권한 변경 실패(관리자)")
    class updateUserRole {
        @Test
        @DisplayName("권한 변경 성공")
        void updateUserRole_success() {
            // given
            long userId = 1L;
            UpdateUserRoleRequest request = new UpdateUserRoleRequest(Role.OWNER);

            when(userRepository.findWithRolesById(userId)).thenReturn(Optional.of(user));

            // when
            userAdminService.updateUserRole(userId, request);

            // then
            assertThat(user.getRoles().contains(Role.OWNER)).isTrue();
        }

        @Test
        @DisplayName("권한 변경 실패 - 존재하지 않는 회원")
        void updateUserRole_fail_when() {
            // given
            long userId = 1L;
            UpdateUserRoleRequest request = new UpdateUserRoleRequest(Role.OWNER);
            when(userRepository.findWithRolesById(eq(userId))).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userAdminService.findUserInfo(userId))
                    .isInstanceOf(UserException.class)
                    .hasMessage(UserErrorCode.NOT_EXIST_USER.getMessage());
        }
    }
}
