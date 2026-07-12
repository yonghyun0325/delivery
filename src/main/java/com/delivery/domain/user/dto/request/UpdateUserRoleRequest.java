package com.delivery.domain.user.dto.request;

import com.delivery.domain.user.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "관리자 회원 권한 요청")
public record UpdateUserRoleRequest(
        @Schema(description = "권한", example = "CUSTOMER") @NotNull(message = "REQUIRED_VALUE")
                Role role) {}
