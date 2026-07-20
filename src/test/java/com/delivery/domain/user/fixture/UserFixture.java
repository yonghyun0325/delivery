package com.delivery.domain.user.fixture;

import com.delivery.domain.user.dto.request.SignUpRequest;
import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.User;
import com.delivery.domain.user.entity.UserStatus;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum UserFixture {
    ROLE_MASTER("test123", "Testtest123!", "닉네임", "01012345678", Role.MASTER),
    ROLE_MANAGER("test123", "Testtest123!", "닉네임", "01012345678", Role.MANAGER),
    ROLE_OWNER("test123", "Testtest123!", "닉네임", "01012345678", Role.OWNER),
    ROLE_CUSTOMER("test123", "Testtest123!", "닉네임", "01012345678", Role.CUSTOMER);

    private final String username;
    private final String password;
    private final String nickname;
    private final String phoneNumber;
    private final Role role;

    // Create DTO
    public SignUpRequest createRequestDto() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return SignUpRequest.builder()
                .username(suffix)
                .password(password)
                .nickName(suffix)
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

    public User createUserNoId(String username, String nickname) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return User.builder()
                .username(username != null ? username : this.username)
                .password(password)
                .nickName(nickname != null ? nickname : this.nickname)
                .phoneNumber(phoneNumber)
                .userStatus(UserStatus.ACTIVE)
                .roles(Set.of(role))
                .build();
    }

    public User createDeletedUserNoId() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return User.builder()
                .username(suffix)
                .password(password)
                .nickName(suffix)
                .phoneNumber(phoneNumber)
                .userStatus(UserStatus.DELETED)
                .roles(Set.of(role))
                .deletedAt(LocalDateTime.now())
                .build();
    }
}
