package com.delivery.global.security.handler;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.user.exception.AuthException;
import com.delivery.domain.user.service.AuthService;
import com.delivery.global.exception.ErrorCode;
import com.delivery.global.exception.GlobalErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
    private final ObjectMapper objectMapper;
    private final AuthService authService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        RestApiResponse logoutResponse = null;
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.OK.value());

        try {
            authService.logout(request);
            logoutResponse =
                    RestApiResponse.success(
                            HttpStatus.OK, "로그아웃 성공", null);

        } catch (AuthException e) {
            ErrorCode errorCode = e.getErrorCode();
            response.setStatus(errorCode.getHttpStatus().value());

            logoutResponse =
                    RestApiResponse.fail(
                            errorCode.getHttpStatus(),
                            errorCode.getMessage(),
                            errorCode.getName());
        }

        try {
            objectMapper.writeValue(response.getWriter(), logoutResponse);
        } catch (IOException e) {
            log.error("[Logout] IOException {}", e.getMessage(), e);
            throw new AuthException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
