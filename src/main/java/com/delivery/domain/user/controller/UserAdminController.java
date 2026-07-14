package com.delivery.domain.user.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.user.dto.request.UpdateUserRoleRequest;

import com.delivery.domain.user.dto.request.UserSearchRequest;
import com.delivery.domain.user.dto.response.PageResponse;
import com.delivery.domain.user.dto.response.UserAdminListResponse;
import com.delivery.domain.user.dto.response.UserAdminResponse;
import com.delivery.domain.user.service.UserAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 관리자 회원 관리 컨트롤러 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
public class UserAdminController {
    private final UserAdminService userAdminService;

    @GetMapping("/{userId}")
    public ResponseEntity<RestApiResponse<UserAdminResponse>> getUserInfo(
            @PathVariable long userId) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK, "회원 정보 조회 성공", userAdminService.findUserInfo(userId)));
    }

    // TODO : API 명세서 수정 POST가 적합
    @PostMapping
    public ResponseEntity<RestApiResponse<PageResponse<UserAdminListResponse>>> getAllUserInfo(
            @RequestBody UserSearchRequest request,
            @ParameterObject
                    @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {

        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "회원 목록 조회 성공",
                        userAdminService.findAllUserInfo(request, pageable)));
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<RestApiResponse<?>> updateUserRole(
            @PathVariable long userId, @Valid @RequestBody UpdateUserRoleRequest request) {
        userAdminService.updateUserRole(userId, request);
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "권한 수정 성공", null));
    }
}
