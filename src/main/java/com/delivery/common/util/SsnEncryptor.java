package com.delivery.common.util;

import com.delivery.global.exception.BusinessException;
import com.delivery.global.exception.GlobalErrorCode;
import java.nio.charset.StandardCharsets;
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
     * @return new String(Base64.encode(encrypt));
     */
    public String encrypt(String ssn) {
        if (ssn == null || ssn.isBlank()) {
            throw new BusinessException(GlobalErrorCode.BAD_REQUEST);
        }
        byte[] encrypt = aesBytesEncryptor.encrypt(ssn.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.encode(encrypt), StandardCharsets.UTF_8);
    }

    /**
     * 복호화
     *
     * @param ssn
     * @return
     */
    public String decrypt(String ssn) {
        if (ssn == null || ssn.isBlank()) {
            throw new BusinessException(GlobalErrorCode.BAD_REQUEST);
        }
        try {
            byte[] decode = Base64.decode(ssn.getBytes(StandardCharsets.UTF_8));
            return new String(aesBytesEncryptor.decrypt(decode), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
