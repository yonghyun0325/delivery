package com.delivery.domain.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegionRequest(
        @NotBlank @Size(min = 1, max = 100) String name,
        @NotNull Double latitude,
        @NotNull Double longitude) {}
