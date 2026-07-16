package com.delivery.common.util;

import com.delivery.global.exception.BusinessException;
import com.delivery.global.exception.GlobalErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

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
    void crypt_success() {
        // given
        String ssn = "01012345678";

        // when
        String encrypted = ssnEncryptor.encrypt(ssn);
        String decrypted = ssnEncryptor.decrypt(encrypted);

        // then
        assertThat(decrypted).isEqualTo(ssn);
        assertThat(encrypted).isNotEqualTo(ssn);
    }

    @NullSource
    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    @DisplayName("암호화 시 Null 값이나 공백이 들어올 시 BAD_REQUEST 예외가 발생한다.")
    void encrpt_fail_null(String ssn) {
        // when & then
        assertThatThrownBy(() ->ssnEncryptor.encrypt(ssn))
                .isInstanceOf(BusinessException.class)
                .hasMessage(GlobalErrorCode.BAD_REQUEST.getMessage());
    }

    @NullSource
    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    @DisplayName("복호화 시 Null 값이나 공백이 들어올 시 BAD_REQUEST 예외가 발생한다.")
    void decrypt_fail_null(String ssn) {
        // when & then
        assertThatThrownBy(() ->ssnEncryptor.decrypt(ssn))
                .isInstanceOf(BusinessException.class)
                .hasMessage(GlobalErrorCode.BAD_REQUEST.getMessage());
    }

    @Test
    @DisplayName("복호화 중 오류가 발생하면 INTERNAL_SERVER_ERROR 예외가 발생한다.")
    void decrypt_fail_internal_server_error() {
        // given
        String encrypted = "test-base64-string";

        // when & then
        assertThatThrownBy(() ->ssnEncryptor.decrypt(encrypted))
                .isInstanceOf(BusinessException.class)
                .hasMessage(GlobalErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    }
}
