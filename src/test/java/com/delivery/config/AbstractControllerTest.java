package com.delivery.config;

import com.delivery.global.cache.BlackListRepository;
import com.delivery.global.cache.RefreshTokenRepository;
import com.delivery.global.cache.UserCacheRepository;
import com.delivery.global.config.JwtProperties;
import com.delivery.global.exception.ErrorCodeRegistry;
import com.delivery.global.exception.GlobalExceptionHandler;
import com.delivery.global.security.config.CustomUserDetailsService;
import com.delivery.global.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 추상 컨트롤러 테스트 클래스
 */
@Import({GlobalExceptionHandler.class, ErrorCodeRegistry.class})
public abstract class AbstractControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected UserCacheRepository userCacheRepository;

    @MockitoBean
    protected RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    protected JwtUtil jwtUtil;

    @MockitoBean protected JwtProperties jwtProperties;

    @MockitoBean
    protected CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    protected BlackListRepository blackListRepository;
}
