package com.delivery.domain.user.dto;

import com.delivery.domain.user.dto.response.AddressResponse;
import com.delivery.domain.user.dto.response.AuthResponse;
import com.delivery.domain.user.dto.response.UserResponse;
import com.delivery.domain.user.entity.Address;
import com.delivery.domain.user.entity.User;
import com.delivery.global.security.config.CustomUserDetails;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserDtoMapper {
    public AuthResponse toDto(CustomUserDetails user, String accessToken) {
        return new AuthResponse(user.getUsername(), user.getNickName(), accessToken);
    }

    public AddressResponse toDto(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getAddress(),
                address.getAddressDetail(),
                address.isDefault());
    }

    public UserResponse toDto(User user) {
        return new UserResponse(
                user.getUsername(),
                user.getNickName(),
                User.maskingPhoneNumber(user.getPhoneNumber()),
                user.getRoles());
    }
}
