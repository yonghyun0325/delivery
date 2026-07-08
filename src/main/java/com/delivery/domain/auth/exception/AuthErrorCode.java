package com.delivery.domain.auth.exception;

import com.delivery.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    // 400 Bad Request
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "MASTER나 MANAGER 권한으로 가입할 수 없습니다."),

    // 401 Unauthorized
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "아이디가 존재하지 않거나 비밀번호가 올바르지 않습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 JWT 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),

    // 403 Forbidden
    NOT_OWNER(HttpStatus.FORBIDDEN, "사장 권한이 없습니다."),
    NOT_MASTER(HttpStatus.FORBIDDEN, "마스터 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getName() {
        return this.name();
    }
}
