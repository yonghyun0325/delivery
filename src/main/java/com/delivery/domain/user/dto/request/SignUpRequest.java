package com.delivery.domain.user.dto.request;

import com.delivery.domain.user.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record SignUpRequest(
        @NotBlank(message = "REQUIRED_VALUE")
                @Size(min = 4, max = 10, message = "INVALID_USERNAME")
                @Pattern(regexp = "^[a-z0-9]+$", message = "INVALID_USERNAME")
                String username,
        @NotBlank(message = "REQUIRED_VALUE")
                @Size(min = 8, max = 15, message = "INVALID_PASSWORD")
                @Pattern(
                        regexp =
                                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]).+$",
                        message = "INVALID_PASSWORD")
                String password,
        @NotBlank(message = "REQUIRED_VALUE")
                @Size(min = 2, max = 11, message = "INVALID_NICKNAME")
                @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "INVALID_NICKNAME")
                String nickName,
        @Size(min = 11, max = 11, message = "INVALID_PHONE_NUMBER")
                @Pattern(regexp = "^010\\d+$", message = "INVALID_PHONE_NUMBER")
                String phoneNumber,
        @NotNull(message = "REQUIRED_VALUE") Role role) {}
