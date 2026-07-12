package com.delivery.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@EnableJpaAuditing(auditorAwareRef = "customAuditorAware")
public class JpaAuditingConfig {

    /*@Bean
    public AuditorAware<String> auditorProvider() {
        // TODO: Spring Security/JWT 적용 후 로그인 사용자 ID 또는 username으로 교체
        return () -> Optional.of("TEMP_USER");
    }*/
}
