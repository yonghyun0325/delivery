package com.delivery.domain.user.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.user.controller.swagger.AuthApi;
import com.delivery.domain.user.dto.request.LoginRequest;
import com.delivery.domain.user.dto.request.SignUpRequest;
import com.delivery.domain.user.dto.response.AuthResponse;
import com.delivery.domain.user.service.AuthService;
import com.delivery.global.security.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.delivery.global.config.JwtProperties.REFRESH_TOKEN_VALIDITY_SECONDS;

/** 인증 / 인가 컨트롤러 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthApi {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<RestApiResponse<String>> signUp(
            @Valid @RequestBody SignUpRequest request) {
        AuthResponse authResponseToken = authService.signUp(request);

        var cookie = ResponseCookie.from("refreshToken", authResponseToken.refreshToken())
                .maxAge(REFRESH_TOKEN_VALIDITY_SECONDS)
                .path("/")
                .secure(false)
                .sameSite("Strict")
                .httpOnly(true)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(
                        RestApiResponse.success(
                                HttpStatus.CREATED, "회원가입 성공", authResponseToken.accessToken()));
    }

    @PostMapping("/login")
    public ResponseEntity<RestApiResponse<String>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse authResponseToken = authService.login(request);

        var cookie = ResponseCookie.from("refreshToken", authResponseToken.refreshToken())
                .maxAge(REFRESH_TOKEN_VALIDITY_SECONDS)
                .path("/")
                .secure(false)
                .sameSite("Strict")
                .httpOnly(true)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(RestApiResponse.success(HttpStatus.OK, "로그인 성공", authResponseToken.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<RestApiResponse<Void>> logout(HttpServletRequest request) {
        authService.logout(request);

        var cookie = ResponseCookie.from("refreshToken", "")
                .maxAge(0)
                .path("/")
                .secure(false)
                .sameSite("Strict")
                .httpOnly(true)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(RestApiResponse.success(HttpStatus.OK, "로그아웃 성공", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RestApiResponse<String>> refreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        AuthResponse authResponseToken = authService.refresh(refreshToken);

        var cookie = ResponseCookie.from("refreshToken", authResponseToken.refreshToken())
                .maxAge(REFRESH_TOKEN_VALIDITY_SECONDS)
                .path("/")
                .secure(false)
                .sameSite("Strict")
                .httpOnly(true)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(RestApiResponse.success(HttpStatus.OK, "Refresh Token 재발급 성공", authResponseToken.accessToken()));
    }
}
