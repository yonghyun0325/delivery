package com.delivery.domain.store.dto.request;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record StoreRequest(
        @NotNull UUID categoryId,
        @NotNull UUID regionId,
        @NotBlank @Size(min = 1, max = 50) String name,
        @NotBlank @Size(max = 255) String address,
        @NotBlank @Size(max = 20) String phone,
        @Size(max = 500) String description,
        @NotNull @Min(0) Integer minOrderAmount) {}
