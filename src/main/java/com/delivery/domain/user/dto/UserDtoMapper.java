package com.delivery.domain.user.dto;

import com.delivery.domain.user.dto.response.*;
import com.delivery.domain.user.entity.Address;
import com.delivery.domain.user.entity.User;
import com.delivery.global.security.config.CustomUserDetails;
import lombok.experimental.UtilityClass;

/** DTO MAPPING 유틸리티 클래스 */
@UtilityClass
public class UserDtoMapper {
    public AuthResponse toAuthResponse(CustomUserDetails user, String accessToken, String refreshToken) {
        return new AuthResponse(user.getUsername(), user.getNickName(), accessToken, refreshToken);
    }

    public AddressResponse toAddressResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getAddress(),
                address.getAddressDetail(),
                address.isDefault());
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getUsername(),
                user.getNickName(),
                User.maskingPhoneNumber(user.getPhoneNumber()),
                user.getRoles());
    }

    public UserAdminResponse toUserAdminResponse(User user) {
        return new UserAdminResponse(
                user.getId(),
                user.getUsername(),
                user.getNickName(),
                user.getPhoneNumber(),
                user.getUserStatus(),
                user.getRoles(),
                user.getCreatedAt(),
                user.getDeletedAt());
    }

    public UserAdminListResponse toUserAdminListResponse(User user) {
        return new UserAdminListResponse(
                user.getId(),
                user.getUsername(),
                user.getNickName(),
                user.getUserStatus(),
                user.getCreatedAt());
    }
}
