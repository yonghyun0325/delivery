package com.delivery.domain.user.fixture;

import com.delivery.domain.user.dto.request.SignUpRequest;
import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.entity.UserStatus;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum UserFixture {
    ROLE_MASTER("test123", "testtest123", "닉네임", "01012345678", Role.MASTER),
    ROLE_MANAGER("test123", "testtest123", "닉네임", "01012345678", Role.MANAGER),
    ROLE_OWNER("test123", "testtest123", "닉네임", "01012345678", Role.OWNER),
    ROLE_CUSTOMER("test123", "testtest123", "닉네임", "01012345678", Role.CUSTOMER);

    private final String username;
    private final String password;
    private final String nickname;
    private final String phoneNumber;
    private final Role role;

    // Create DTO
    public SignUpRequest createRequestDto() {
        return SignUpRequest.builder()
                .username(username)
                .password(password)
                .nickName(nickname)
                .phoneNumber(phoneNumber)
                .role(role)
                .build();
    }

    public User createUser(long userId) {
        return User.builder()
                .id(userId)
                .username(username)
                .password(password)
                .nickName(nickname)
                .phoneNumber(phoneNumber)
                .userStatus(UserStatus.ACTIVE)
                .roles(Set.of(role))
                .build();
    }

    public User createUserNoId() {
        return User.builder()
                .username(username)
                .password(password)
                .nickName(nickname)
                .phoneNumber(phoneNumber)
                .userStatus(UserStatus.ACTIVE)
                .roles(Set.of(role))
                .build();
    }

    public User createDeletedUserNoId() {
        return User.builder()
                .username(username)
                .password(password)
                .nickName(nickname)
                .phoneNumber(phoneNumber)
                .userStatus(UserStatus.DELETED)
                .roles(Set.of(role))
                .deletedAt(LocalDateTime.now())
                .build();
    }
}
