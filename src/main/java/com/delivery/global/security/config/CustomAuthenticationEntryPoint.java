package com.delivery.global.security.config;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.user.exception.AuthErrorCode;
import com.delivery.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException {
        ErrorCode errorCode = AuthErrorCode.TOKEN_NOT_FOUND;
        HttpStatus httpStatus = errorCode.getHttpStatus();
        String message = errorCode.getMessage();
        String error = errorCode.getName();

        log.warn(
                "사용자 인증 실패 - ErrorCode : {}, ErrorMessage : {}",
                errorCode.getName(),
                errorCode.getMessage(),
                authException);

        response.setStatus(httpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        var errorResponse = RestApiResponse.fail(httpStatus, message, error);

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
