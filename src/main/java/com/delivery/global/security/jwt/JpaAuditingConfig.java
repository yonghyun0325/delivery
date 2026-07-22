package com.delivery.global.security.jwt;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@EnableJpaAuditing(auditorAwareRef = "customAuditorAware")
public class JpaAuditingConfig {}
