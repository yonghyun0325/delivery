package com.delivery.domain.user.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 2, max = 11, message = "INVALID_NICKNAME")
                @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "INVALID_NICKNAME")
                String nickName,
        @Size(min = 11, max = 11, message = "INVALID_PHONE_NUMBER")
                @Pattern(regexp = "^010\\d+$", message = "INVALID_PHONE_NUMBER")
                String phoneNumber) {}
