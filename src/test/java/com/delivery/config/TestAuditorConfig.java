package com.delivery.config;

import com.delivery.common.util.CryptoConverter;
import com.delivery.common.util.SsnEncryptor;
import com.delivery.global.config.CustomAuditorAware;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;

@TestConfiguration
public class TestAuditorConfig {
    private final String KEY = "8qzv8E0VZEtnmloiNYDAmj/KJz2E7GeT5Hg02C3bi6o=";
    private final String SALT = "e81814d3cd292fcfaf6f47822eacb9e8";
    private final AesBytesEncryptor encryptor = new AesBytesEncryptor(KEY, SALT);

    @Bean
    public SsnEncryptor ssnEncryptor() {
        return new SsnEncryptor(encryptor);
    }

    @Bean
    public CryptoConverter cryptoConverter(SsnEncryptor ssnEncryptor) {
        return new CryptoConverter(ssnEncryptor);
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return new CustomAuditorAware();
    }
}
