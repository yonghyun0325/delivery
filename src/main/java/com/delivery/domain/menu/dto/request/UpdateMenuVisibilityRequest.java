package com.delivery.domain.menu.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateMenuVisibilityRequest(@NotNull Boolean hidden) {}
