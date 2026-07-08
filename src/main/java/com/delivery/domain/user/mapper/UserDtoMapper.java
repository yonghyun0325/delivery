package com.delivery.domain.user.mapper;

import com.delivery.domain.auth.dto.AuthResponseDto;
import com.delivery.domain.user.dto.AddressResponseDto;
import com.delivery.domain.user.entity.Address;
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

    public AddressResponseDto toDto(Address address) {
        return AddressResponseDto.builder()
                .addressId(address.getId())
                .address(address.getAddress())
                .addressDetail(address.getAddressDetail())
                .isDefault(address.isDefault())
                .build();
    }
}
