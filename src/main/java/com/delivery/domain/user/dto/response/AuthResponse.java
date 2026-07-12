package com.delivery.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record AuthResponse(
        @Schema(description = "아이디", example = "test1234") String username,
        @Schema(description = "닉네임", example = "닉네임") String nickName,
        @Schema(description = "엑세스토큰", example = "Token") String accessToken) {}
