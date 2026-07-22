package com.delivery.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.delivery.config.AbstractJpaTest;
import com.delivery.config.CustomDataJpaTest;
import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@CustomDataJpaTest
class UserRepositoryTest extends AbstractJpaTest {
    @Autowired private UserRepository userRepository;
    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(UserFixture.ROLE_CUSTOMER.createUserNoId(null, null));
    }

    @Nested
    @DisplayName("아이디 존재 여부 확인")
    class existsByUsername {
        @Test
        @DisplayName("해당 아이디가 존재하면 true를 반환한다..")
        void existsByUsername_exist() {
            // when
            boolean result = userRepository.existsByUsername(savedUser.getUsername());

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("해당 아이디가 없으면 false를 반환한다.")
        void existsByUsername_not_found() {
            // when
            boolean result = userRepository.existsByUsername(savedUser.getUsername() + "fail");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("닉네임 존재 여부 확인")
    class existsByNickName {
        @Test
        @DisplayName("해당 닉네임이 존재하면 true를 반환한다.")
        void existsByNickName_exist() {
            // when
            boolean result = userRepository.existsByNickName(savedUser.getNickName());

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("해당 닉네임이 없으면 false를 반환한다.")
        void existsByNickName_not_found() {
            // when
            boolean result = userRepository.existsByNickName(savedUser.getNickName() + "fail");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("특정 아이디를 가진 사용자를 찾는다.")
    class findByUsername {
        @Test
        @DisplayName("특정 아이디를 가진 사용자를 조회한다.")
        void findByUsername_success() {
            // when
            var result = userRepository.findByUsername(savedUser.getUsername());

            // then
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("특정 아이디를 가진 사용자를 찾을 수 없는 경우 조회되지 않는다..")
        void findByUsername_fail() {
            // when
            var result = userRepository.findByUsername(savedUser.getUsername() + "fail");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("삭제되지 않은 userId(PK) 단건 조회.")
    class findByIdAndDeletedAtIsNull {
        @Test
        @DisplayName("삭제되지 않은 특정 사용자를 조회한다.")
        void findByIdAndDeletedAtIsNull_find() {
            // when
            var result = userRepository.findByIdAndDeletedAtIsNull(savedUser.getId());

            // then
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("삭제된 특정 사용자는 조회되지 않는다..")
        void findByIdAndDeletedAtIsNull_not_found() {
            // when
            savedUser.delete(savedUser.getUsername());
            var result = userRepository.findByIdAndDeletedAtIsNull(savedUser.getId());

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("삭제되지 않은 사용자를 username를 사용해 권한과 함께 조회한다.")
    class findWithRolesByUsernameAndDeletedAtIsNull {
        @Test
        @DisplayName("특정 사용자를 조회한다.")
        void findWithRolesByUsernameAndDeletedAtIsNull_find() {
            // when
            var result =
                    userRepository
                            .findWithRolesByUsernameAndDeletedAtIsNull(savedUser.getUsername())
                            .orElseThrow();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getRoles()).contains(Role.CUSTOMER);
        }

        @Test
        @DisplayName("삭제된 특정 사용자는 조회되지 않는다..")
        void findWithRolesByUsernameAndDeletedAtIsNull_not_found() {
            // when
            savedUser.delete(savedUser.getUsername());
            var result =
                    userRepository.findWithRolesByUsernameAndDeletedAtIsNull(
                            savedUser.getUsername());

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("삭제되지 않은 사용자를 userUuid를 사용해 권한과 함께 조회한다.")
    class findWithRolesByUserUuidAndDeletedAtIsNull {
        @Test
        @DisplayName("특정 사용자를 조회한다.")
        void findWithRolesByUserUuidAndDeletedAtIsNull_find() {
            // when
            var result =
                    userRepository
                            .findWithRolesByUserUuidAndDeletedAtIsNull(savedUser.getUserUuid())
                            .orElseThrow();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getRoles()).contains(Role.CUSTOMER);
        }

        @Test
        @DisplayName("삭제된 특정 사용자는 조회되지 않는다..")
        void findWithRolesByUserUuidAndDeletedAtIsNull_not_found() {
            // when
            savedUser.delete(savedUser.getUsername());
            var result =
                    userRepository.findWithRolesByUserUuidAndDeletedAtIsNull(
                            savedUser.getUserUuid());

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("삭제되지 않은 사용자를 userId를 사용해 권한과 함께 조회한다.")
    class findWithRolesByIdAndDeletedAtIsNull {
        @Test
        @DisplayName("특정 사용자를 조회한다.")
        void findWithRolesByIdAndDeletedAtIsNull_find() {
            // when
            var result =
                    userRepository
                            .findWithRolesByIdAndDeletedAtIsNull(savedUser.getId())
                            .orElseThrow();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getRoles()).contains(Role.CUSTOMER);
        }

        @Test
        @DisplayName("삭제된 특정 사용자는 조회되지 않는다..")
        void findWithRolesByIdAndDeletedAtIsNull_not_found() {
            // when
            savedUser.delete(savedUser.getUsername());
            var result = userRepository.findWithRolesByIdAndDeletedAtIsNull(savedUser.getId());

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("사용자를 userId를 사용해 권한과 함께 조회한다.")
    class findWithRolesById {
        @Test
        @DisplayName("특정 사용자를 조회한다.")
        void findWithRolesById_find() {
            // when
            var result = userRepository.findWithRolesById(savedUser.getId()).orElseThrow();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getRoles()).contains(Role.CUSTOMER);
        }
    }
}
