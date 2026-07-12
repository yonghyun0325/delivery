package com.delivery.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "닉네임 수정 요청")
public record UpdateNickNameRequest(
        @Size(min = 2, max = 11, message = "INVALID_NICKNAME")
                @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "INVALID_NICKNAME")
                String nickName) {}
