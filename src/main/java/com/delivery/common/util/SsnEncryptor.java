package com.delivery.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.stereotype.Component;

/** 양방향 암호화 유틸리티 전화번호, 주소, 이메일 등 개인정보 암복호화 */
@Component
@RequiredArgsConstructor
public class SsnEncryptor {
    private final AesBytesEncryptor aesBytesEncryptor;

    /**
     * 암호화
     *
     * @param ssn
     * @return new String(Base64.encode(encrypt)); TODO : null 체크
     */
    public String encrypt(String ssn) {
        byte[] encrypt = aesBytesEncryptor.encrypt(ssn.getBytes());
        return new String(Base64.encode(encrypt));
    }

    /**
     * 복호화
     *
     * @param ssn
     * @return TODO : null 체크
     */
    public String decrypt(String ssn) {
        byte[] decode = Base64.decode(ssn.getBytes());
        return new String(aesBytesEncryptor.decrypt(decode));
    }
}
