package com.delivery.domain.auth.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.auth.dto.AuthResponseDto;
import com.delivery.domain.auth.dto.LoginRequestDto;
import com.delivery.domain.auth.dto.SignUpRequestDto;
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

    @PostMapping
    public ResponseEntity<RestApiResponse<AuthResponseDto>> signUp(
            @Valid @RequestBody SignUpRequestDto request) {
        AuthResponseDto response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestApiResponse.success(HttpStatus.CREATED, "회원가입 성공", response));
    }

    @PostMapping("/login")
    public ResponseEntity<RestApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request) {
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(RestApiResponse.success(HttpStatus.OK, "로그인 성공", response));
    }
}
