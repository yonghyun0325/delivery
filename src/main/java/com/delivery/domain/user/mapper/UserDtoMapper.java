package com.delivery.domain.user.mapper;

import com.delivery.domain.auth.dto.AuthResponseDto;
import com.delivery.global.security.config.CustomUserDetails;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserDtoMapper {
    public AuthResponseDto toDto(CustomUserDetails user, String accessToken) {
        return AuthResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickName(user.getNickName())
                .accessToken(accessToken)
                .build();
    }
}
