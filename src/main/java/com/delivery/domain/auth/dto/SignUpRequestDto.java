package com.delivery.domain.auth.dto;

import com.delivery.domain.user.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @Size(min = 4, max = 10)
    @Pattern(regexp = "^[a-z0-9]+$")
    private String username;

    @NotBlank
    @Size(min = 8, max = 15)
    @Pattern(
            regexp =
                    "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]).+$")
    private String password;

    @NotBlank
    @Size(min = 2, max = 16)
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$")
    private String nickName;

    @Size(min = 11, max = 11)
    @Pattern(regexp = "^010\\d+$")
    private String phoneNumber;

    // TODO : 권한 검증 로직 필요
    @NotNull private Role role;

    private String createdBy = "SYSTEM";
}
