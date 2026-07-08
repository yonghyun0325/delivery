package com.delivery.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponseDto {
    private Long id;
    private String username;
    private String nickName;
    private String accessToken;
}
