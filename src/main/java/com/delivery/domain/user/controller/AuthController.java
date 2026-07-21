package com.delivery.domain.user.controller;

import static com.delivery.global.config.JwtProperties.REFRESH_TOKEN_VALIDITY_SECONDS;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.user.controller.swagger.AuthApi;
import com.delivery.domain.user.dto.request.LoginRequest;
import com.delivery.domain.user.dto.request.SignUpRequest;
import com.delivery.domain.user.dto.response.AuthResponse;
import com.delivery.domain.user.service.AuthService;
import com.delivery.global.config.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 인증 / 인가 컨트롤러 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthApi {
    private final AuthService authService;
    private final JwtProperties jwtProperties;

    @PostMapping
    public ResponseEntity<RestApiResponse<String>> signUp(
            @Valid @RequestBody SignUpRequest request) {
        AuthResponse authResponseToken = authService.signUp(request);

        String accessToken = authResponseToken.accessToken();
        String refreshToken = authResponseToken.refreshToken();

        var cookie = createRefreshToken(refreshToken, REFRESH_TOKEN_VALIDITY_SECONDS);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(RestApiResponse.success(HttpStatus.CREATED, "회원가입 성공", accessToken));
    }

    @PostMapping("/login")
    public ResponseEntity<RestApiResponse<String>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponseToken = authService.login(request);

        String accessToken = authResponseToken.accessToken();
        String refreshToken = authResponseToken.refreshToken();

        var cookie = createRefreshToken(refreshToken, REFRESH_TOKEN_VALIDITY_SECONDS);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(RestApiResponse.success(HttpStatus.OK, "로그인 성공", accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<RestApiResponse<Void>> logout(HttpServletRequest request) {
        authService.logout(request);

        var cookie = createRefreshToken("", 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(RestApiResponse.success(HttpStatus.OK, "로그아웃 성공", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RestApiResponse<String>> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        AuthResponse authResponseToken = authService.refresh(refreshToken);

        String accessToken = authResponseToken.accessToken();
        String newRefreshToken = authResponseToken.refreshToken();

        var cookie = createRefreshToken(newRefreshToken, REFRESH_TOKEN_VALIDITY_SECONDS);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(RestApiResponse.success(HttpStatus.OK, "Refresh Token 재발급 성공", accessToken));
    }

    private ResponseCookie createRefreshToken(String refreshToken, long maxAge) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .maxAge(maxAge)
                .path("/")
                .secure(jwtProperties.isCookieSecure())
                .sameSite("Strict")
                .httpOnly(true)
                .build();
    }
}
