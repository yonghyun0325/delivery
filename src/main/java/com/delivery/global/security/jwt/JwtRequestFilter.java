package com.delivery.global.security.jwt;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.user.exception.AuthErrorCode;
import com.delivery.global.cache.BlackListRepository;
import com.delivery.global.cache.RefreshTokenRepository;
import com.delivery.global.cache.UserCacheRepository;
import com.delivery.global.exception.ErrorCode;
import com.delivery.global.security.config.CustomUserDetails;
import com.delivery.global.security.config.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper;
    private final BlackListRepository blackListRepository;
    private final UserCacheRepository userCacheRepository;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String accessToken = jwtUtil.resolveAccessToken(request);
        ErrorCode errorCode  = null;
        String username = null;

        if (accessToken != null) {
            try {
                if (blackListRepository.findByKey(accessToken) != null) {
                    errorCode = AuthErrorCode.BLACKLISTED_TOKEN;
                    setErrorResponse(response, errorCode);
                    log.warn(AuthErrorCode.BLACKLISTED_TOKEN.getMessage());
                    return;
                }
                username = jwtUtil.getUserUsernameFromToken(accessToken);
                UUID userUuid = jwtUtil.getUserUuidFromAccessToken(accessToken);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    CustomUserDetails userDetails = userCacheRepository.findByKey(userUuid);

                    if (userDetails == null) {
                        userDetails = customUserDetailsService.loadUserByUuid(userUuid);
                        userCacheRepository.save(userUuid, userDetails);
                        log.debug("Jwt 캐싱 {} : {}", userUuid, userDetails);
                    }

                    if (jwtUtil.validateToken(accessToken, userDetails)) {
                        setAuthentication(request, userDetails);
                    } else {
                        errorCode = AuthErrorCode.INVALID_ACCESS_TOKEN;
                        setErrorResponse(response, errorCode);
                        logger.warn(errorCode.getMessage());
                        return;
                    }
                }

            } catch (IllegalArgumentException e) {
                errorCode = AuthErrorCode.INVALID_ACCESS_TOKEN;
                setErrorResponse(response, errorCode);
                logger.warn(errorCode.getMessage(), e);
            } catch (ExpiredJwtException e) {
                errorCode = AuthErrorCode.EXPIRED_ACCESS_TOKEN;
                setErrorResponse(response, errorCode);
                logger.warn(errorCode.getMessage(), e);
            }
        } else {
            logger.debug("Authorization Header가 없습니다.");
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

        usernamePasswordAuthenticationToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
    }

    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        var errorResponse = RestApiResponse.fail(
                errorCode.getHttpStatus(),
                errorCode.getMessage(),
                errorCode.getName()
        );

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
