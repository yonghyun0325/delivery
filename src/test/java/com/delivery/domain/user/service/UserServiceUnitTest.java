package com.delivery.domain.user.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.delivery.domain.user.dto.request.UpdateNickNameRequest;
import com.delivery.domain.user.dto.request.UpdatePhoneNumberRequest;
import com.delivery.domain.user.dto.response.UserValidationResponse;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.exception.UserException;
import com.delivery.domain.user.fixture.UserFixture;
import com.delivery.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {
    @Mock private UserRepository userRepository;
    @InjectMocks private UserService userService;

    @Test
    @DisplayName("회원 정보를 조회 할 수 없는 경우 NOT_EXIST_USER 예외를 발생시킨다.")
    void findUserInfo_fail_when_() {
        // given
        long userId = 1L;
        when(userRepository.findWithRolesByIdAndDeletedAtIsNull(eq(userId)))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findUserInfo(userId))
                .isInstanceOf(UserException.class)
                .hasMessage("존재하지 않는 회원입니다.");
    }

    @Nested
    @DisplayName("회원 정보 수정")
    class UpdateUser {
        @Test
        @DisplayName("닉네임 변경 시 회원 정보를 찾을 수 없는 경우 NOT_EXIST_USER 예외를 반환한다..")
        void updateNickName_fail_when_exist_user() {
            // given
            long userId = 1L;
            UpdateNickNameRequest request = new UpdateNickNameRequest("닉네임");

            when(userRepository.findByIdAndDeletedAtIsNull(eq(userId)))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.updateNickName(userId, request))
                    .isInstanceOf(UserException.class)
                    .hasMessage("존재하지 않는 회원입니다.");
        }

        @Test
        @DisplayName("닉네임 변경 시 중복인 경우 DUPLICATE_NICKNAME 예외를 발생시킨다.")
        void updateNickName_fail_when_duplicate() {
            // given
            User user = UserFixture.ROLE_CUSTOMER.createUser(1L);
            UpdateNickNameRequest request = new UpdateNickNameRequest("닉네임1");

            when(userRepository.findByIdAndDeletedAtIsNull(eq(user.getId())))
                    .thenReturn(Optional.of(user));
            when(userRepository.existsByNickName(eq(request.nickName()))).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.updateNickName(user.getId(), request))
                    .isInstanceOf(UserException.class)
                    .hasMessage("이미 사용중인 닉네임입니다.");
        }

        @Test
        @DisplayName("닉네임이 기존과 동일한 경우 변경 하지않고 완료 처리 한다.")
        void updateNickName_skip_when_same() {
            // given
            User user = UserFixture.ROLE_CUSTOMER.createUser(1L);
            UpdateNickNameRequest request = new UpdateNickNameRequest(user.getNickName());

            when(userRepository.findByIdAndDeletedAtIsNull(eq(user.getId())))
                    .thenReturn(Optional.of(user));

            // when
            userService.updateNickName(user.getId(), request);

            // then
            verify(userRepository, never()).existsByNickName(anyString());
        }

        @Test
        @DisplayName("연락처 변경 시 회원 정보를 찾을 수 없는 경우 NOT_EXIST_USER 예외를 반환한다.")
        void updatePhoneNumber_fail_when_exist_user() {
            // given
            long userId = 1L;
            UpdatePhoneNumberRequest request = new UpdatePhoneNumberRequest("01012345678");

            when(userRepository.findByIdAndDeletedAtIsNull(eq(userId)))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.updatePhoneNumber(userId, request))
                    .isInstanceOf(UserException.class)
                    .hasMessage("존재하지 않는 회원입니다.");
        }
    }

    @Test
    @DisplayName("회원 삭제 시 회원 정보를 찾을 수 없는 경우 NOT_EXIST_USER 예외를 반환한다.")
    void deleteUser_fail_when_exist_user() {
        // given
        long userId = 1L;
        when(userRepository.findByIdAndDeletedAtIsNull(eq(userId))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(UserException.class)
                .hasMessage("존재하지 않는 회원입니다.");
    }

    @Nested
    @DisplayName("중복체크")
    class duplicate {
        @Test
        @DisplayName("아이디가 중복인 경우 true 반환")
        void isDuplicationUsername_success() {
            // given
            String username = "test1234";
            when(userRepository.existsByUsername(eq(username))).thenReturn(true);

            // when
            UserValidationResponse response = userService.isDuplicationUsername(username);

            // then
            assertThat(response.isDuplicated()).isTrue();
        }

        @Test
        @DisplayName("아이디가 중복이 아닌 경우 false 반환")
        void isDuplicationUsername_fail_when_duplicated() {
            // given
            String username = "test1234";
            when(userRepository.existsByUsername(eq(username))).thenReturn(false);

            // when
            UserValidationResponse response = userService.isDuplicationUsername(username);

            // then
            assertThat(response.isDuplicated()).isFalse();
        }

        @Test
        @DisplayName("닉네임이 중복인 경우 true 반환")
        void isDuplicationNickname_success() {
            // given
            String nickName = "닉네임";
            when(userRepository.existsByNickName(eq(nickName))).thenReturn(true);

            // when
            UserValidationResponse response = userService.isDuplicationNickname(nickName);

            // then
            assertThat(response.isDuplicated()).isTrue();
        }

        @Test
        @DisplayName("닉네임이 중복이 아닌 경우 false 반환")
        void isDuplicationNickname_fail_when_duplicated() {
            // given
            String nickName = "닉네임";
            when(userRepository.existsByNickName(eq(nickName))).thenReturn(false);

            // when
            UserValidationResponse response = userService.isDuplicationNickname(nickName);

            // then
            assertThat(response.isDuplicated()).isFalse();
        }
    }

    @Test
    @DisplayName("활성화 회원이 아닌 경우 NOT_EXIST_USER 예외를 밚환한다.")
    void findActiveUser_fail_when_not_exist_user() {
        // given
        long userId = 1L;
        when(userRepository.findByIdAndDeletedAtIsNull(eq(userId))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findActiveUser(userId))
                .isInstanceOf(UserException.class)
                .hasMessage("존재하지 않는 회원입니다.");
    }
}
