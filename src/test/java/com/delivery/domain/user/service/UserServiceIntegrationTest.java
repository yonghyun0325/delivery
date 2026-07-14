package com.delivery.domain.user.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.delivery.config.AbstractIntegrationTest;
import com.delivery.domain.user.dto.request.SignUpRequest;
import com.delivery.domain.user.dto.request.UpdateNickNameRequest;
import com.delivery.domain.user.dto.request.UpdatePhoneNumberRequest;
import com.delivery.domain.user.dto.response.UserResponse;
import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.entity.UserStatus;
import com.delivery.domain.user.fixture.UserFixture;
import com.delivery.domain.user.repository.UserRepository;
import com.delivery.global.config.JwtProperties;
import jakarta.persistence.EntityManager;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class UserServiceIntegrationTest extends AbstractIntegrationTest {
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EntityManager entityManager;
    @Autowired private JwtProperties jwtProperties;

    private User savedUser;
    private long userId;
    private String username;

    @BeforeEach
    void setUp() {
        SignUpRequest request = UserFixture.ROLE_CUSTOMER.createRequestDto();

        String encodedPassword = passwordEncoder.encode(request.password());
        Set<Role> roles = Role.getDefaultRoles(request.role());

        savedUser =
                userRepository.save(
                        User.create(
                                request.username(),
                                encodedPassword,
                                request.nickName(),
                                request.phoneNumber(),
                                roles));
        userId = savedUser.getId();
        username = savedUser.getUsername();
    }

    @Test
    @DisplayName("회원 정보 조회")
    void findUserInfo_success() {
        // when
        UserResponse actual = userService.findUserInfo(userId);

        UserResponse expected =
                new UserResponse(
                        savedUser.getUsername(),
                        savedUser.getNickName(),
                        savedUser
                                        .getPhoneNumber()
                                        .substring(0, savedUser.getPhoneNumber().length() - 4)
                                + "****",
                        savedUser.getRoles());

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Nested
    @DisplayName("회원 정보 수정")
    class updateUser {
        @Test
        @DisplayName("닉네임 수정 성공")
        void updateNickName_success() {
            // given
            UpdateNickNameRequest request = new UpdateNickNameRequest("닉네임2");

            // when
            userService.updateNickName(userId, request);

            entityManager.flush();
            entityManager.clear();

            var actual = userRepository.findById(userId).orElseThrow();

            // then
            assertThat(actual.getNickName()).isEqualTo(request.nickName());
        }

        @Test
        @DisplayName("연락처 수정 성공")
        void updatePhoneNumber_success() {
            // given
            UpdatePhoneNumberRequest request = new UpdatePhoneNumberRequest("01087654321");

            // when
            userService.updatePhoneNumber(userId, request);

            entityManager.flush();
            entityManager.clear();

            var actual = userRepository.findById(userId).orElseThrow();

            // then
            assertThat(actual.getPhoneNumber()).isEqualTo(request.phoneNumber());
        }
    }

    @Test
    @DisplayName("회원 삭제 성공")
    void deleteUser_success() {
        // when
        userService.deleteUser(userId);
        var actual = userRepository.findById(userId).orElseThrow();

        // then
        assertThat(actual.getDeletedAt()).isNotNull();
        assertThat(actual.getUserStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(actual.getUsername()).startsWith(username + "_");
        assertThat(actual.getNickName()).startsWith("탈퇴회원" + "_");
        assertThat(actual.getDeletedBy()).startsWith(userId + "_" + username);
    }
}
