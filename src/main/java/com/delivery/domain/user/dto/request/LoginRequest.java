package com.delivery.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청")
public record LoginRequest(
        @NotBlank(message = "REQUIRED_USER_ID") @Schema(description = "아이디", example = "test1234")
                String username,
        @NotBlank(message = "REQUIRED_PASSWORD")
                @Schema(description = "비밀번호", example = "Test12345!")
                String password) {}
