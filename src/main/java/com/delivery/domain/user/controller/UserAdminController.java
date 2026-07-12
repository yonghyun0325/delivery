package com.delivery.domain.user.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.user.dto.request.UpdateUserRoleRequest;
import com.delivery.domain.user.dto.response.UserAdminListResponse;
import com.delivery.domain.user.dto.response.UserAdminResponse;
import com.delivery.domain.user.service.UserAdminService;
import com.delivery.global.security.config.CustomUserDetails;
import jakarta.validation.Valid;
import java.awt.print.Pageable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** 관리자 회원 관리 컨트롤러 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admihn/users")
public class UserAdminController {
    private final UserAdminService userAdminService;

    @GetMapping
    public ResponseEntity<RestApiResponse<UserAdminResponse>> getUserInfo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "회원 정보 조회 성공",
                        userAdminService.findUserInfo(customUserDetails.getId())));
    }

    @GetMapping("/{userId}") // TODO : 조건 좀 생각해볼 것, pageble 커스텀?
    public ResponseEntity<RestApiResponse<List<UserAdminListResponse>>> getAllUserInfo(
            Pageable pageable) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK, "회원 목록 조회 성공", userAdminService.findAllUserInfo()));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<RestApiResponse<?>> updateUserRole(
            @PathVariable long userId, @Valid UpdateUserRoleRequest request) {
        userAdminService.updateUserRole(userId, request);
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "권한 수정 성공", null));
    }
}
