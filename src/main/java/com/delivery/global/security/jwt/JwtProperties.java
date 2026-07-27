package com.delivery.global.security.jwt;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String accessSecret, String refreshSecret, boolean cookieSecure) {
    public static final long ACCESS_TOKEN_VALIDITY = Duration.ofMinutes(30).toMillis();
    public static final long REFRESH_TOKEN_VALIDITY = Duration.ofDays(14).toMillis();
    public static final long REFRESH_TOKEN_VALIDITY_SECONDS = Duration.ofDays(14).toSeconds();
}
