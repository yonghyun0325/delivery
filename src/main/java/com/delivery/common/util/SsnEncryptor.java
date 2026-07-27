package com.delivery.common.util;

import com.delivery.global.exception.BusinessException;
import com.delivery.global.exception.GlobalErrorCode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Component;

/** 양방향 암호화 유틸리티 전화번호, 주소, 이메일 등 개인정보 암복호화 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SsnEncryptor {
    private final BytesEncryptor bytesEncryptor;

    /**
     * 암호화
     *
     * @param ssn
     * @return new String(Base64.encode(encrypt));
     */
    public String encrypt(String ssn) {
        if (ssn == null || ssn.isBlank()) {
            log.warn("[SsnEncryptor] Encrypt failed: {}", GlobalErrorCode.BAD_REQUEST);
            throw new BusinessException(GlobalErrorCode.BAD_REQUEST);
        }
        byte[] encrypt = bytesEncryptor.encrypt(ssn.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.getEncoder().encode(encrypt), StandardCharsets.UTF_8);
    }

    /**
     * 복호화
     *
     * @param ssn
     * @return
     */
    public String decrypt(String ssn) {
        if (ssn == null || ssn.isBlank()) {
            log.warn("[SsnEncryptor] Decrypt failed: {}", GlobalErrorCode.BAD_REQUEST);
            throw new BusinessException(GlobalErrorCode.BAD_REQUEST);
        }
        try {
            byte[] decode = Base64.getDecoder().decode(ssn.getBytes(StandardCharsets.UTF_8));
            return new String(bytesEncryptor.decrypt(decode), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("[SsnEncryptor] Decrypt failed: {}", e.getMessage(), e);
            throw new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
