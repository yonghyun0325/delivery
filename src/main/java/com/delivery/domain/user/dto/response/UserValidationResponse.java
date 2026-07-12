package com.delivery.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "중복 체크 응답")
public record UserValidationResponse(
        @Schema(description = "중복 여부", example = "true") boolean isDuplicated) {}
