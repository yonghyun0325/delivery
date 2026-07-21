package com.delivery.global.security.config;

import com.delivery.global.security.handler.*;
import com.delivery.global.security.jwt.JwtAuthenticationService;
import com.delivery.global.security.jwt.JwtRequestFilter;
import com.delivery.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity httpSecurity,
            JwtRequestFilter jwtRequestFilter,
            AccessDeniedHandler accessDeniedHandler,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint)
            throws Exception {
        // csrf 비활성화
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        // 세션 STATELESS
        httpSecurity.sessionManagement(
                sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 필터 관리
        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        // 예외 핸들러 설정
        httpSecurity.exceptionHandling(
                config ->
                        config.authenticationEntryPoint(customAuthenticationEntryPoint)
                                .accessDeniedHandler(accessDeniedHandler));

        // URL 인가 설정
        httpSecurity.authorizeHttpRequests(
                (requests) ->
                        // Swagger
                        requests.requestMatchers(
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/scalar",
                                        "/scalar/**",
                                        "/webjars/**")
                                .permitAll()

                                // 공통 권한
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/v1/auth",
                                        "/api/v1/auth/login",
                                        "/api/v1/auth/refresh")
                                .permitAll()

                                // 중복 체크
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/users/check-id",
                                        "/api/v1/users/check-nickname")
                                .permitAll()

                                // 기본 권한
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/stores",
                                        "/api/v1/stores/*",
                                        "/api/v1/stores/*/review-summary",
                                        "/api/v1/stores/*/reviews",
                                        "/api/v1/stores/*/ratings")
                                .permitAll()

                                // 카테고리, 지역 권한
                                .requestMatchers(
                                        HttpMethod.GET, "/api/v1/categories", "/api/v1/regions")
                                .permitAll()

                                // 관리자
                                .requestMatchers("/api/v1/admin/users/**")
                                .hasAnyRole("MANAGER")

                                // 나머지
                                .anyRequest()
                                .authenticated());

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {

        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CustomAccessDeniedHandler customAccessDeniedHandler(JsonMapper jsonMapper) {
        return new CustomAccessDeniedHandler(jsonMapper);
    }

    @Bean
    public CustomAuthenticationEntryPoint customAuthenticationEntryPoint(JsonMapper jsonMapper) {
        return new CustomAuthenticationEntryPoint(jsonMapper);
    }

    @Bean
    public JwtRequestFilter jwtRequestFilter(
            JwtAuthenticationService jwtAuthenticationService,
            JsonMapper jsonMapper,
            JwtUtil jwtUtil) {
        return new JwtRequestFilter(jwtAuthenticationService, jsonMapper, jwtUtil);
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy(
                "ROLE_MASTER > ROLE_MANAGER > ROLE_OWNER > ROLE_CUSTOMER");
    }
}
