package com.delivery.domain.auth.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.auth.dto.request.LoginRequest;
import com.delivery.domain.auth.dto.request.SignUpRequest;
import com.delivery.domain.auth.dto.response.AuthResponse;
import com.delivery.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * TODO : 전화번호는 개인정보므로 양방향 암호화로 변경해야함, DTO에 뒷번호 4자리 가린 상태로 프론트로 전달 관리자는 상관없이 볼 수 있어야함. 전화번호 컬럼길이
     * 수정할 것
     *
     * @param request
     * @return
     */
    @PostMapping
    public ResponseEntity<RestApiResponse<AuthResponse>> signUp(
            @Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        RestApiResponse.success(
                                HttpStatus.CREATED, "회원가입 성공", authService.signUp(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<RestApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "로그인 성공", authService.login(request)));
    }
}
