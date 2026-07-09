package com.delivery.domain.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateMenuRequest(
        @NotBlank @Size(max = 100) String name, String description, @Positive int price) {}
