package com.delivery.global.security.jwt;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.user.exception.AuthErrorCode;
import com.delivery.domain.user.exception.AuthException;
import com.delivery.domain.user.exception.UserException;
import com.delivery.global.exception.ErrorCode;
import com.delivery.global.security.principal.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtAuthenticationService authenticationService;
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String accessToken = jwtUtil.resolveAccessToken(request);

        if (accessToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                CustomUserDetails userDetails = authenticationService.authenticate(accessToken);
                setAuthentication(request, userDetails);
            } catch (ExpiredJwtException e) {
                handleErrorResponse(response, AuthErrorCode.EXPIRED_ACCESS_TOKEN, e);
                return;
            } catch (AuthException | UserException e) {
                handleErrorResponse(response, e.getErrorCode(), e);
                return;
            } catch (IllegalArgumentException | JwtException e) {
                handleErrorResponse(response, AuthErrorCode.INVALID_ACCESS_TOKEN, e);
                return;
            }
        } else {
            logger.debug("Authorization Header가 없습니다.");
        }
        filterChain.doFilter(request, response);
    }

    // 컨택스트 생성
    private void setAuthentication(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

        usernamePasswordAuthenticationToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
    }

    // Response 규격 통일, 따로 분리 가능성 있음
    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode)
            throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        var errorResponse =
                RestApiResponse.fail(
                        errorCode.getHttpStatus(), errorCode.getMessage(), errorCode.getName());

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    private void handleErrorResponse(HttpServletResponse response, ErrorCode errorCode, Exception e)
            throws IOException {
        setErrorResponse(response, errorCode);
        log.warn(errorCode.getMessage(), e);
    }
}
