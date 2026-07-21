package com.delivery.global.config;

import java.time.Duration;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    public static final long ACCESS_TOKEN_VALIDITY = Duration.ofMinutes(30).toMillis();
    public static final long REFRESH_TOKEN_VALIDITY = Duration.ofDays(14).toMillis();
    public static final long REFRESH_TOKEN_VALIDITY_SECONDS = Duration.ofDays(14).toSeconds();
    private final String accessSecret;
    private final String refreshSecret;
    private final boolean cookieSecure;

    public JwtProperties(String accessSecret, String refreshSecret, boolean cookieSecure) {
        this.accessSecret = accessSecret;
        this.refreshSecret = refreshSecret;
        this.cookieSecure = cookieSecure;
    }
}
