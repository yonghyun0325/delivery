package com.delivery.global.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;

/** 양방향 암호화 설정 전화번호, 주소 등에 개인정보 암호화 Entity에 @Convert(converter = CryptoConverter.class) 붙여 사용 */
@Configuration
public class EncryptConfig {
    @Value("${encrypt.key}")
    private String key;

    @Value("${encrypt.salt}")
    private String salt;

    @Bean
    public AesBytesEncryptor aesBytesEncryptor() throws Exception {
        return new AesBytesEncryptor(key, salt);
    }
}
