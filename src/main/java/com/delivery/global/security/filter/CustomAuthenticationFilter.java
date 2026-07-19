package com.delivery.global.security.filter;

import com.delivery.domain.user.dto.request.LoginRequest;
import com.delivery.domain.user.exception.AuthException;
import com.delivery.global.exception.GlobalErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * UsernamePasswordAuthenticationFilter 로그인 커스텀 필터
 */
@Slf4j
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager) { setAuthenticationManager(authenticationManager); }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

            String username = loginRequest.username();
            String password = loginRequest.password();

            // 토큰 생성
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

            // AuthenticationManager에게 인증 처리 위임
            return getAuthenticationManager().authenticate(authRequest);

        } catch (IOException e) {
            log.error("[Login] IOException {}", e.getMessage(), e);
            throw new AuthException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
