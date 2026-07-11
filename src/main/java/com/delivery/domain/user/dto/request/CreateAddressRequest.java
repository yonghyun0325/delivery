package com.delivery.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateAddressRequest(
        @NotBlank(message = "REQUIRED_VALUE") String address,
        @NotBlank(message = "REQUIRED_VALUE") String addressDetail,
        @NotNull(message = "REQUIRED_VALUE") Boolean isDefault) {}
