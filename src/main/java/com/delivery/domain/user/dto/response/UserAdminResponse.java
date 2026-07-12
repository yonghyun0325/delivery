package com.delivery.domain.user.dto.response;

import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "관리자 회원 조회 응답")
public record UserAdminResponse(
        @Schema(description = "회원 고유키", example = "1") long userId,
        @Schema(description = "아이디", example = "test1234") String username,
        @Schema(description = "닉네임", example = "닉네임") String nickName,
        @Schema(description = "연락처", example = "01012345678") String phoneNumber,
        @Schema(description = "회원상태", example = "ACTIVE") UserStatus userStatus,
        @Schema(description = "권한", example = "[\"CUSTOMER\"]") Set<Role> roles,
        @Schema(description = "생성일자", example = "2026-07-12T19:15:30") LocalDateTime createdAt,
        @Schema(description = "삭제일자", example = "2026-07-12T19:15:30") LocalDateTime deletedAt) {}
