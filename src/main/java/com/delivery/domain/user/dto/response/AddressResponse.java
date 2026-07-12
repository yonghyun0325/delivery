package com.delivery.domain.user.dto.response;

import java.util.UUID;

public record AddressResponse(
        UUID addressId, String address, String addressDetail, boolean isDefault) {}
