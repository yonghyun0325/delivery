package com.delivery.global.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.delivery.domain.auth.exception.AuthErrorCode;
import com.delivery.domain.user.exception.UserErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ErrorCodeRegistryTest {
    private final ErrorCodeRegistry errorCodeRegistry = new ErrorCodeRegistry();

    @Test
    @DisplayName("등록된 에러코드를 조회하면 해당 ErrorCode가 Return 되어야 한다.")
    void resolve_return_error_code() {
        // given
        String errorCode = "NOT_FOUND";
        String errorCode2 = "INVALID_USERNAME";
        String errorCode3 = "TOKEN_NOT_FOUND";

        // when
        ErrorCode globalErrorCode = errorCodeRegistry.getByName(errorCode);
        ErrorCode userErrorCode = errorCodeRegistry.getByName(errorCode2);
        ErrorCode authErrorCode = errorCodeRegistry.getByName(errorCode3);

        // then
        assertThat(globalErrorCode.getName()).isEqualTo(GlobalErrorCode.NOT_FOUND.getName());
        assertThat(globalErrorCode.getMessage()).isEqualTo(GlobalErrorCode.NOT_FOUND.getMessage());
        assertThat(userErrorCode.getName()).isEqualTo(UserErrorCode.INVALID_USERNAME.getName());
        assertThat(userErrorCode.getMessage())
                .isEqualTo(UserErrorCode.INVALID_USERNAME.getMessage());
        assertThat(authErrorCode.getName()).isEqualTo(AuthErrorCode.TOKEN_NOT_FOUND.getName());
        assertThat(authErrorCode.getMessage())
                .isEqualTo(AuthErrorCode.TOKEN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("등록되지 않은 에러코드를 조회하면 GlobalErrorCode BAD_REQUEST(400)이 Return 되어야 한다.")
    void resolve_invalid_return_notFound() {
        // given
        String errorCode = "실제 존재하지 않는 에러코드";

        // when
        ErrorCode unknownErrorCode = errorCodeRegistry.getByName(errorCode);

        // then
        assertThat(unknownErrorCode.getName()).isEqualTo(GlobalErrorCode.BAD_REQUEST.getName());
        assertThat(unknownErrorCode.getMessage())
                .isEqualTo(GlobalErrorCode.BAD_REQUEST.getMessage());
    }
}
