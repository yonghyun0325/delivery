package com.delivery.domain.user.dto.request;

import com.delivery.domain.user.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "회원가입 요청")
public record SignUpRequest(
        @NotBlank(message = "REQUIRED_VALUE")
                @Size(min = 4, max = 10, message = "INVALID_USERNAME")
                @Pattern(regexp = "^[a-z0-9]+$", message = "INVALID_USERNAME")
                @Schema(description = "아이디", example = "test1234")
                String username,
        @NotBlank(message = "REQUIRED_VALUE")
                @Size(min = 8, max = 15, message = "INVALID_PASSWORD")
                @Pattern(
                        regexp =
                                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]).+$",
                        message = "INVALID_PASSWORD")
                @Schema(description = "비밀번호", example = "Test12345!")
                String password,
        @NotBlank(message = "REQUIRED_VALUE")
                @Size(min = 2, max = 11, message = "INVALID_NICKNAME")
                @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "INVALID_NICKNAME")
                @Schema(description = "닉네임", example = "닉네임")
                String nickName,
        @Size(min = 11, max = 11, message = "INVALID_PHONE_NUMBER")
                @Pattern(regexp = "^010\\d+$", message = "INVALID_PHONE_NUMBER")
                @Schema(description = "연락처", example = "01012345678")
                String phoneNumber,
        @Schema(description = "권한", example = "CUSTOMER") @NotNull(message = "REQUIRED_VALUE")
                Role role) {}
