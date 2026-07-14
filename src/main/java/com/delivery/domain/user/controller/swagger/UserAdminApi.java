package com.delivery.domain.user.controller.swagger;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.user.dto.request.UpdateUserRoleRequest;
import com.delivery.domain.user.dto.request.UserSearchRequest;
import com.delivery.domain.user.dto.response.PageResponse;
import com.delivery.domain.user.dto.response.UserAdminListResponse;
import com.delivery.domain.user.dto.response.UserAdminResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "관리자 회원 관리", description = "회원 관리 관련 API")
public interface UserAdminApi {
    @Operation(summary = "회원 정보 조회", description = "관리자가 특정 회원을 조회 합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원 정보 조회 성공"),
        @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
        @ApiResponse(responseCode = "403", description = "접근 권한이 없습니다."),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다."),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<RestApiResponse<UserAdminResponse>> getUserInfo(
            @PathVariable long userId);

    @Operation(summary = "회원 목록 조회", description = "관리자가 회원 목록을 조회 합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원 정보 조회 성공"),
        @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
        @ApiResponse(responseCode = "403", description = "접근 권한이 없습니다."),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다."),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<RestApiResponse<PageResponse<UserAdminListResponse>>> getAllUserInfo(
            @ModelAttribute UserSearchRequest request, @ModelAttribute Pageable pageable);

    @Operation(summary = "회원 권한 수정", description = "관리자가 회원의 권한을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원 권한 수정 성공"),
        @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
        @ApiResponse(responseCode = "403", description = "접근 권한이 없습니다."),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다."),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/{userId}/role")
    public ResponseEntity<RestApiResponse<?>> updateUserRole(
            @PathVariable long userId, @Valid UpdateUserRoleRequest request);
}
