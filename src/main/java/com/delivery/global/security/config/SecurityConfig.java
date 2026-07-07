package com.delivery.global.security.config;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
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
                        requests.requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v1/auth")
                                .permitAll()
                                .requestMatchers("/h2-console/**")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout")
                                .permitAll()
                                .anyRequest()
                                .authenticated());
        //        httpSecurity
        //                .exceptionHandling(config -> config
        //                        .authenticationEntryPoint(
        //                                jwtAuthenticationEntryPoint));

        //        httpSecurity.sessionManagement(
        //                sessionManagement ->
        //
        // sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        //        // 필터 관리
        //        httpSecurity.addFilterBefore(jwtRequestFilter,
        // UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }
}
