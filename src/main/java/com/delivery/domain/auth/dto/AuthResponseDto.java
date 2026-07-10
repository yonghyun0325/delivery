package com.delivery.domain.auth.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthResponseDto {
    private Long id;
    private String username;
    private String nickName;
    private String accessToken;
}
