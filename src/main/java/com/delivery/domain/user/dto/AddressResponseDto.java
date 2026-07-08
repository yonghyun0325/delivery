package com.delivery.domain.user.dto;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressResponseDto {
    private UUID addressId;
    private String address;
    private String addressDetail;
    boolean isDefault;
}
