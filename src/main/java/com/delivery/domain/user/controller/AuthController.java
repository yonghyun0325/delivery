package com.delivery.domain.user.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.user.controller.swagger.AuthApi;
import com.delivery.domain.user.dto.request.LoginRequest;
import com.delivery.domain.user.dto.request.SignUpRequest;
import com.delivery.domain.user.dto.response.AuthResponse;
import com.delivery.domain.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인증 / 인가 컨트롤러 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthApi {
    private final AuthService authService;

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

    @PostMapping("/logout")
    public ResponseEntity<RestApiResponse<Void>> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "로그아웃 성공", null));
    }


}
