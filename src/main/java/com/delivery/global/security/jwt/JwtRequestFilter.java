package com.delivery.global.security.jwt;

import com.delivery.domain.user.exception.AuthErrorCode;
import com.delivery.domain.user.exception.AuthException;
import com.delivery.global.cache.BlackListRepository;
import com.delivery.global.cache.RefreshTokenRepository;
import com.delivery.global.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final RefreshTokenRepository refreshTokenRepository ;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String accessToken = jwtUtil.resolveAccessToken(request);
        String username = null;

        if (accessToken != null) {
            try {
                UUID userUuid = jwtUtil.getUserUuidFromAccessToken(accessToken);

                if(!refreshTokenRepository.findByKey(userUuid).isPresent()) {
                    throw new AuthException(AuthErrorCode.BLACKLISTED_TOKEN);
                }
                username = jwtUtil.getUserUsernameFromToken(accessToken);
            } catch (IllegalArgumentException e) {
                ErrorCode errorCode = AuthErrorCode.INVALID_ACCESS_TOKEN;
                logger.warn(errorCode.getMessage(), e);
            } catch (ExpiredJwtException e) {
                ErrorCode errorCode = AuthErrorCode.EXPIRED_TOKEN;
                logger.warn(errorCode.getMessage(), e);
            }
        } else {
            logger.debug("Authorization Header가 없습니다.");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(accessToken, userDetails)) {
                setAuthentication(request, userDetails);
            } else {
                ErrorCode errorCode = AuthErrorCode.INVALID_ACCESS_TOKEN;
                logger.warn(errorCode.getMessage());
            }
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
}
