package com.delivery.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.delivery.config.AbstractIntegrationTest;
import com.delivery.domain.user.dto.UserDtoMapper;
import com.delivery.domain.user.dto.request.UpdateUserRoleRequest;
import com.delivery.domain.user.dto.request.UserSearchRequest;
import com.delivery.domain.user.dto.response.PageResponse;
import com.delivery.domain.user.dto.response.UserAdminListResponse;
import com.delivery.domain.user.dto.response.UserAdminResponse;
import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.entity.UserStatus;
import com.delivery.domain.user.exception.UserErrorCode;
import com.delivery.domain.user.exception.UserException;
import com.delivery.domain.user.fixture.UserFixture;
import com.delivery.domain.user.repository.UserRepository;
import com.delivery.global.cache.UserCacheRepository;
import com.delivery.global.exception.BusinessException;
import com.delivery.global.exception.GlobalErrorCode;
import com.delivery.global.security.principal.CustomUserDetails;
import com.delivery.global.security.principal.CustomUserDetailsService;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class UserAdminServiceIntegrationTest extends AbstractIntegrationTest {
    @Autowired private UserAdminService userAdminService;
    @Autowired private UserRepository userRepository;
    @Autowired private CustomUserDetailsService customUserDetailsService;
    @Autowired private UserCacheRepository userCacheRepository;
    private UserAdminResponse savedUser;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAllInBatch();
        savedUser =
                UserDtoMapper.toUserAdminResponse(
                        userRepository.save(
                                UserFixture.ROLE_CUSTOMER.createUserNoId(null, "닉네임1")));
        userRepository.save(UserFixture.ROLE_CUSTOMER.createUserNoId("test1134", "닉네임2"));
        userRepository.save(UserFixture.ROLE_CUSTOMER.createUserNoId("test1145", "닉네임3"));
        userRepository.save(UserFixture.ROLE_CUSTOMER.createUserNoId("test1146", "닉네임4"));
        userRepository.save(UserFixture.ROLE_OWNER.createUserNoId("test2234", "가게주인1"));
        userRepository.save(UserFixture.ROLE_OWNER.createUserNoId("test2235", "가게주인2"));
        userRepository.save(UserFixture.ROLE_OWNER.createUserNoId("test2236", "가게주인3"));
        userRepository.save(UserFixture.ROLE_CUSTOMER.createDeletedUserNoId());
        userRepository.save(UserFixture.ROLE_CUSTOMER.createDeletedUserNoId());
    }

    @Nested
    @DisplayName("회원 단건 조회")
    class findUserInfo {
        @Test
        @DisplayName("회원 단건 조회 성공")
        void findUserInfo_success() {
            // when
            var findUser = userAdminService.findUserInfo(savedUser.userId());

            // then
            assertThat(findUser).isEqualTo(savedUser);
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 UserException(NOT_EXIST_USER) 예외를 발생시킨다.")
        void findUserInfo_fail_when_not_found() {
            // when & then
            assertThatThrownBy(() -> userAdminService.findUserInfo(10l))
                    .isInstanceOf(UserException.class)
                    .hasMessage(UserErrorCode.NOT_EXIST_USER.getMessage());
        }
    }

    @Nested
    @DisplayName("회원 목록 조회")
    class findAllUsers {
        @ParameterizedTest
        @MethodSource("searchRequest")
        @DisplayName("회원 목록 조회 성공")
        void findAllUserInfo_success(UserSearchRequest request, int count) {
            // given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

            // when
            PageResponse<UserAdminListResponse> response =
                    userAdminService.findAllUserInfo(request, pageable);

            // them
            assertThat(response.totalElements()).isEqualTo(count);
        }

        private static Stream<Arguments> searchRequest() {
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);

            return Stream.of(
                    Arguments.of(new UserSearchRequest(null, null, null, null, null), 9),
                    Arguments.of(new UserSearchRequest(Role.CUSTOMER, null, null, null, null), 6),
                    Arguments.of(new UserSearchRequest(null, null, "test11", null, null), 3),
                    Arguments.of(
                            new UserSearchRequest(null, UserStatus.DELETED, null, null, null), 2),
                    Arguments.of(
                            new UserSearchRequest(Role.OWNER, UserStatus.ACTIVE, null, null, null),
                            3),
                    Arguments.of(
                            new UserSearchRequest(null, UserStatus.ACTIVE, null, today, null), 7),
                    Arguments.of(
                            new UserSearchRequest(
                                    Role.CUSTOMER, UserStatus.DELETED, null, null, tomorrow),
                            2),
                    Arguments.of(new UserSearchRequest(null, null, null, today, tomorrow), 9));
        }

        @Test
        @DisplayName("StartDate보다 endDate가 미래인 경우 예외를 반환한다..")
        void findAllUserInfo_fail_when_range_date() {
            // given
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);

            var request = new UserSearchRequest(null, null, null, tomorrow, today);
            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

            // when
            assertThatThrownBy(() -> userAdminService.findAllUserInfo(request, pageable))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(GlobalErrorCode.INVALID_DATE_RANGE.getMessage());
        }
    }

    @Nested
    @DisplayName("회원 권한 수정")
    class updateUserRole {
        @Test
        @DisplayName("회원 권한 수정 성공 시 회원 권한을 업데이트 하고 회원 캐싱 데이터를 지운다.")
        void updateUserRole_success() {
            // given
            UpdateUserRoleRequest request = new UpdateUserRoleRequest(Role.MANAGER);
            CustomUserDetails userDetails =
                    customUserDetailsService.loadUserByUuid(savedUser.uuid());
            userCacheRepository.save(savedUser.uuid(), userDetails);

            // when
            userAdminService.updateUserRole(savedUser.userId(), request);
            User updatedUser = userRepository.findById(savedUser.userId()).orElseThrow();

            // then
            assertThat(userCacheRepository.findByKey(savedUser.uuid())).isNull();
            assertThat(updatedUser.getRoles()).contains(Role.CUSTOMER, Role.OWNER, Role.MANAGER);
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 UserException(NOT_EXIST_USER) 예외가 발생한다.")
        void updateUserRole_fail_when_not_found() {
            // given
            long userId = 1000L;
            UpdateUserRoleRequest request = new UpdateUserRoleRequest(Role.OWNER);

            // when & then
            assertThatThrownBy(() -> userAdminService.updateUserRole(userId, request))
                    .isInstanceOf(UserException.class)
                    .hasMessage(UserErrorCode.NOT_EXIST_USER.getMessage());
        }
    }
}
