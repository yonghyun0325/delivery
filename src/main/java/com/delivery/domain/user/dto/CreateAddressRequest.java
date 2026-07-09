package com.delivery.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateAddressRequest {
    @NotBlank private String address;
    @NotBlank private String addressDetail;
    @NotNull private Boolean isDefault;
}
