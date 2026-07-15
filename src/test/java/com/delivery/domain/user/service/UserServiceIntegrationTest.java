package com.delivery.domain.user.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.delivery.config.AbstractIntegrationTest;
import com.delivery.domain.user.dto.UserDtoMapper;
import com.delivery.domain.user.dto.request.SignUpRequest;
import com.delivery.domain.user.dto.request.UpdateNickNameRequest;
import com.delivery.domain.user.dto.request.UpdatePhoneNumberRequest;
import com.delivery.domain.user.dto.response.UserResponse;
import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.entity.UserStatus;
import com.delivery.domain.user.fixture.UserFixture;
import com.delivery.domain.user.repository.UserRepository;
import com.delivery.global.cache.UserCacheRepository;
import com.delivery.global.config.JwtProperties;
import com.delivery.global.security.config.CustomUserDetailsService;
import jakarta.persistence.EntityManager;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.*;
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
    @Autowired private UserCacheRepository userCacheRepository;
    @Autowired private CustomUserDetailsService customUserDetailsService;

    private User savedUser;
    private long userId;
    private String username;

    @BeforeEach
    void setUp() {
        SignUpRequest baseRequest = UserFixture.ROLE_CUSTOMER.createRequestDto();

        String encodedPassword = passwordEncoder.encode(baseRequest.password());
        Set<Role> roles = Role.getDefaultRoles(baseRequest.role());
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        SignUpRequest request =
                new SignUpRequest(
                        baseRequest.username() + uuid,
                        baseRequest.password(),
                        baseRequest.nickName() + uuid,
                        baseRequest.phoneNumber(),
                        baseRequest.role());

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
        SignUpRequest baseRequest = UserFixture.ROLE_CUSTOMER.createRequestDto();
        String encodedPassword = passwordEncoder.encode(baseRequest.password());
        Set<Role> roles = Role.getDefaultRoles(baseRequest.role());
        User user = User.create("test7654", encodedPassword, "삭제회원닉네임", "01012345678", roles);

        // When
        User savedUser = userRepository.save(user);
        var before = UserDtoMapper.toUserResponse(savedUser);
        Long userId = savedUser.getId();

        userService.deleteUser(userId);
        var after = UserDtoMapper.toUserResponse(userRepository.findById(userId).orElseThrow());

        // 3. Then
        assertThat(before).isNotEqualTo(after);
    }
}
