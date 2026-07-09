package com.delivery.domain.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateMenuRequest(
        @NotBlank @Size(max = 100) String name,
        String description,
        @Positive int price,
        @NotNull Boolean aiGeneration,
        String aiPrompt) {}
