package com.delivery.domain.user.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.user.controller.swagger.UserApi;
import com.delivery.domain.user.dto.request.UpdateNickNameRequest;
import com.delivery.domain.user.dto.request.UpdatePhoneNumberRequest;
import com.delivery.domain.user.dto.response.UserResponse;
import com.delivery.domain.user.dto.response.UserValidationResponse;
import com.delivery.domain.user.service.UserService;
import com.delivery.global.security.config.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** 회원 컨트롤러 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@PreAuthorize("isAuthenticated()")
public class UserController implements UserApi {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<RestApiResponse<UserResponse>> getUserInfo(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "회원 정보 조회 성공",
                        userService.findUserInfo(customUserDetails.getId())));
    }

    @PatchMapping("/me/nickname") // TODO : API 문서 수정필요
    public ResponseEntity<RestApiResponse<UserResponse>> updateNickName(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody UpdateNickNameRequest request) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "회원 정보 수정 성공",
                        userService.updateNickName(customUserDetails.getId(), request)));
    }

    @PatchMapping("/me/phone-number") // TODO : API 문서 수정필요
    public ResponseEntity<RestApiResponse<UserResponse>> updatePhoneNumber(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody UpdatePhoneNumberRequest request) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "회원 정보 수정 성공",
                        userService.updatePhoneNumber(customUserDetails.getId(), request)));
    }

    @DeleteMapping("/me")
    public ResponseEntity<RestApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        userService.deleteUser(customUserDetails.getId());
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "회원 탈퇴 성공", null));
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/check-id")
    public ResponseEntity<RestApiResponse<UserValidationResponse>> isDuplicationUsername(
            @RequestParam String username) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "아이디 중복 체크 성공.",
                        userService.isDuplicationUsername(username)));
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/check-nickname")
    public ResponseEntity<RestApiResponse<UserValidationResponse>> isDuplicationNickname(
            @RequestParam String nickName) {
        return ResponseEntity.ok(
                RestApiResponse.success(
                        HttpStatus.OK,
                        "닉네임 중복 체크 성공",
                        userService.isDuplicationNickname(nickName)));
    }
}
