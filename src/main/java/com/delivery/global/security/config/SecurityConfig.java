package com.delivery.global.security.config;

import com.delivery.global.security.jwt.JwtAuthenticationEntryPoint;
import com.delivery.global.security.jwt.JwtRequestFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;
    private final AccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        httpSecurity.authorizeHttpRequests(
                (requests) ->
                        requests.requestMatchers(
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/scalar",
                                        "/scalar/**",
                                        "/webjars/**")
                                .permitAll()

                                // 공통 권한
                                .requestMatchers(
                                        HttpMethod.POST, "/api/v1/auth", "/api/v1/auth/login")
                                .permitAll()
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/users/check-id",
                                        "/api/v1/users/check-nickname")
                                .permitAll()
                                .requestMatchers(
                                        HttpMethod.GET, "/api/v1/stores", "/api/v1/stores/*")
                                .permitAll()

                                // 관리자
                                .requestMatchers("/api/v1/users", "/api/v1/users/*")
                                .hasAnyRole("MASTER", "MANAGER")

                                // 나머지
                                .anyRequest()
                                .authenticated());
        httpSecurity.exceptionHandling(
                config ->
                        config.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                .accessDeniedHandler(accessDeniedHandler));

        httpSecurity.sessionManagement(
                sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // 필터 관리
        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }
}
