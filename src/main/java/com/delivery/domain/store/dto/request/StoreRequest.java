package com.delivery.domain.store.dto.request;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record StoreRequest(
        @NotNull(message = "REQUIRED_VALUE") UUID categoryId,
        @NotNull(message = "REQUIRED_VALUE") UUID regionId,
        @NotBlank(message = "REQUIRED_VALUE")
                @Size(min = 1, max = 50, message = "INVALID_STORE_NAME")
                String name,
        @NotBlank(message = "REQUIRED_VALUE") @Size(max = 255, message = "INVALID_ADDRESS")
                String address,
        @NotBlank(message = "REQUIRED_VALUE") @Size(max = 20, message = "INVALID_PHONE")
                String phone,
        @Size(max = 500, message = "INVALID_DESCRIPTION") String description,
        @NotNull(message = "REQUIRED_VALUE") @Min(value = 0, message = "INVALID_MIN_ORDER_AMOUNT")
                Integer minOrderAmount) {}
