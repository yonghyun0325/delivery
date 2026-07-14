package com.delivery.domain.store.dto.request;

import jakarta.validation.constraints.NotNull;

public record StoreStatusRequest(@NotNull(message = "REQUIRED_VALUE") Boolean isOpen) {}