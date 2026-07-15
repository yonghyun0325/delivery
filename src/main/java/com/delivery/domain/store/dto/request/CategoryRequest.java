package com.delivery.domain.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "REQUIRED_VALUE")
                @Size(min = 1, max = 50, message = "INVALID_CATEGORY_NAME")
                String name) {}
