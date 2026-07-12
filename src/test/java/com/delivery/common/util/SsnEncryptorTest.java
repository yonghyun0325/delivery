package com.delivery.common.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;

class SsnEncryptorTest {
    private SsnEncryptor ssnEncryptor;
    private final String KEY = "8qzv8E0VZEtnmloiNYDAmj/KJz2E7GeT5Hg02C3bi6o=";
    private final String SALT = "e81814d3cd292fcfaf6f47822eacb9e8";

    @BeforeEach
    void setUp() {
        AesBytesEncryptor encryptor = new AesBytesEncryptor(KEY, SALT);
        this.ssnEncryptor = new SsnEncryptor(encryptor);
    }

    @Test
    @DisplayName("암복호화가 정상적으로 작동해야한다.")
    void encrypt() {
        // given
        String ssn = "01012345678";

        // when
        String encrypted = ssnEncryptor.encrypt(ssn);
        String decrypted = ssnEncryptor.decrypt(encrypted);

        // then
        assertThat(decrypted).isEqualTo(ssn);
        assertThat(encrypted).isNotEqualTo(ssn);
    }
}
