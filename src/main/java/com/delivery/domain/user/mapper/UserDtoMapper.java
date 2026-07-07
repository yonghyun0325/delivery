package com.delivery.domain.user.mapper;

import com.delivery.domain.auth.dto.AuthResponseDto;
import com.delivery.domain.user.entity.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserDtoMapper {
    public AuthResponseDto toDto(User user, String accessToken) {
        return AuthResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickName())
                .accessToken(accessToken)
                .build();
    }
}
