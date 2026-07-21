package com.delivery.config;

import com.delivery.global.security.jwt.JwtProperties;
import com.delivery.global.exception.ErrorCodeRegistry;
import com.delivery.global.exception.GlobalExceptionHandler;
import com.delivery.global.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

/** 추상 컨트롤러 테스트 클래스 */
@EnableConfigurationProperties(JwtProperties.class)
@Import({GlobalExceptionHandler.class, ErrorCodeRegistry.class})
public abstract class AbstractControllerTest {
    @Autowired protected MockMvc mockMvc;

    @Autowired protected JsonMapper jsonMapper;

    @MockitoBean protected JwtUtil jwtUtil;
}
