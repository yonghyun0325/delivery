package com.delivery.global.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "encrypt")
public record EncryptProperties(String key, String salt) {}
