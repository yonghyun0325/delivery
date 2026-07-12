package com.delivery.domain.user.dto.response;

import com.delivery.domain.user.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "회원 정보 응답")
public record UserResponse(
        @Schema(description = "아이디", example = "test1234") String username,
        @Schema(description = "닉네임", example = "닉네임") String nickName,
        @Schema(description = "연락처", example = "0101234****") String phoneNumber,
        @Schema(description = "권한", example = "[\"CUSTOMER\"]") Set<Role> roles) {}
