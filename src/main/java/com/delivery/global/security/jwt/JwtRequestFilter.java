package com.delivery.global.security.jwt;

import com.delivery.domain.auth.exception.AuthErrorCode;
import com.delivery.global.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

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
    private final UserDetailsService jwtUserDetailService;
    private final JwtUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = jwtTokenUtil.resolveToken(request);
        String username = null;

        if (jwt != null) {
            try {
                username = jwtTokenUtil.getUserUsernameFromToken(jwt);
            } catch (IllegalArgumentException e) {
                ErrorCode errorCode = AuthErrorCode.INVALID_TOKEN;
                logger.warn(errorCode.getMessage(), e);
            } catch (ExpiredJwtException e) {
                ErrorCode errorCode = AuthErrorCode.EXPIRED_TOKEN;
                logger.warn(errorCode.getMessage(), e);
            }
        } else {
            logger.debug("Authorization Header가 없습니다.");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = jwtUserDetailService.loadUserByUsername(username);

            if (jwtTokenUtil.validateToken(jwt, userDetails)) {
                setAuthentication(request, userDetails);
            } else {
                ErrorCode errorCode = AuthErrorCode.INVALID_TOKEN;
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