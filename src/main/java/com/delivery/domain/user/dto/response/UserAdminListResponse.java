package com.delivery.domain.user.dto.response;

import com.delivery.domain.user.entity.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "관리자 회원 목록 조회 응답")
public record UserAdminListResponse(
        @Schema(description = "회원 고유키", example = "1") long userId,
        @Schema(description = "아이디", example = "test1234") String username,
        @Schema(description = "닉네임", example = "닉네임") String nickName,
        @Schema(description = "회원상태", example = "ACTIVE") UserStatus userStatus,
        @Schema(description = "생성일자", example = "2026-07-12T19:15:30") LocalDateTime createdAt) {}
