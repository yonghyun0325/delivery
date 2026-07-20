package com.delivery.config;

import com.delivery.global.exception.ErrorCodeRegistry;
import com.delivery.global.exception.GlobalExceptionHandler;
import com.delivery.global.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** 추상 컨트롤러 테스트 클래스 */
@Import({GlobalExceptionHandler.class, ErrorCodeRegistry.class})
public abstract class AbstractControllerTest {
    @Autowired protected MockMvc mockMvc;

    @Autowired protected ObjectMapper objectMapper;

    @MockitoBean protected JwtUtil jwtUtil;
}
