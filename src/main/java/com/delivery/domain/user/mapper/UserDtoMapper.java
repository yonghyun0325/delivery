package com.delivery.domain.user.mapper;

import com.delivery.domain.auth.dto.response.AuthResponse;
import com.delivery.domain.user.dto.response.AddressResponse;
import com.delivery.domain.user.entity.Address;
import com.delivery.global.security.config.CustomUserDetails;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserDtoMapper {
    public AuthResponse toDto(CustomUserDetails user, String accessToken) {
        return new AuthResponse(user.getId(), user.getUsername(), user.getNickName(), accessToken);
    }

    public AddressResponse toDto(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getAddress(),
                address.getAddressDetail(),
                address.isDefault());
    }
}
