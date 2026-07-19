package com.delivery.global.security.handler;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.user.dto.response.AuthResponse;
import com.delivery.domain.user.service.AuthService;
import com.delivery.global.security.principal.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

import static com.delivery.global.config.JwtProperties.REFRESH_TOKEN_VALIDITY_SECONDS;

@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler  {
    private final ObjectMapper objectMapper;

    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        AuthResponse loginResponse = authService.login(customUserDetails);

        var cookie = ResponseCookie.from("refreshToken", loginResponse.refreshToken())
                .maxAge(REFRESH_TOKEN_VALIDITY_SECONDS)
                .path("/")
                .secure(false)
                .sameSite("Strict")
                .httpOnly(true)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        var authResponse =
                RestApiResponse.success(
                        HttpStatus.OK, "로그인 성공", loginResponse.accessToken());

        objectMapper.writeValue(response.getWriter(), authResponse);
    }
}
