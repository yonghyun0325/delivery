package com.delivery.global.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;

/** 양방향 암호화 설정 전화번호, 주소 등에 개인정보 암호화 Entity에 @Convert(converter = CryptoConverter.class) 붙여 사용 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(EncryptProperties.class)
public class EncryptConfig {
    private final EncryptProperties encryptProperties;

    @Bean
    public BytesEncryptor bytesEncryptor() {
        return Encryptors.stronger(encryptProperties.key(), encryptProperties.salt());
    }
}
