package com.delivery.domain.user.fixture;

import com.delivery.domain.auth.dto.request.SignUpRequest;
import com.delivery.domain.user.enums.Role;
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
}
