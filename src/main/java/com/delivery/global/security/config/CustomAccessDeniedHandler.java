package com.delivery.global.security.config;

import com.delivery.common.RestApiResponse;
import com.delivery.global.exception.ErrorCode;
import com.delivery.global.exception.GlobalErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException {
        ErrorCode errorCode = GlobalErrorCode.FORBIDDEN;
        HttpStatus httpStatus = errorCode.getHttpStatus();
        String message = errorCode.getMessage();
        String error = errorCode.getName();

        log.warn(
                "ErrorCode : {}, ErrorMessage : {}",
                errorCode.getName(),
                errorCode.getMessage(),
                accessDeniedException);

        response.setStatus(httpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        var errorResponse = RestApiResponse.fail(httpStatus, message, error);

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
