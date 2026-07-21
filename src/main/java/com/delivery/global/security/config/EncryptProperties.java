package com.delivery.global.security.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "encrypt")
public class EncryptProperties {
    private final String key;
    private final String salt;

    public EncryptProperties(String key, String salt) {
        this.key = key;
        this.salt = salt;
    }
}
