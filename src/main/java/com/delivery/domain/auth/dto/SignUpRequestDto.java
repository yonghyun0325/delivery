package com.delivery.domain.auth.dto;

import com.delivery.domain.user.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDto {
    @NotBlank
    @Size(min = 8, max = 20)
    @Pattern(regexp = "^[a-z0-9]{4,10}$", message = "INVALID_USERNAME")
    @NotBlank
    private String username;

    @Pattern(
            regexp =
                    "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]{8,15}$",
            message = "INVALID_PASSWORD")
    private String password;

    private String nickName;

    @Pattern(regexp = "^[0-9-]{9,15}$", message = "INVALID_PHONE_NUMBER")
    private String phoneNumber;

    private Role role;
}
