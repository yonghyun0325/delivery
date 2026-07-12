package com.delivery.domain.auth.dto.request;

import com.delivery.domain.user.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record SignUpRequest(
        @NotBlank(message = "REQUIRED_VALUE")
                @Size(min = 4, max = 10)
                @Pattern(regexp = "^[a-z0-9]+$")
                String username,
        @NotBlank(message = "REQUIRED_VALUE")
                @Size(min = 8, max = 15)
                @Pattern(
                        regexp =
                                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]).+$")
                String password,
        @NotBlank(message = "REQUIRED_VALUE")
                @Size(min = 2, max = 16)
                @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$")
                String nickName,
        @Size(min = 11, max = 11) @Pattern(regexp = "^010\\d+$") String phoneNumber,
        @NotNull(message = "REQUIRED_VALUE") Role role) {}
