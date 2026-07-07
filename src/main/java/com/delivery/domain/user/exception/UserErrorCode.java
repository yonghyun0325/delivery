package com.delivery.domain.user.exception;

import com.delivery.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    // 400 Bad Request
    INVALID_USERNAME(
            HttpStatus.BAD_REQUEST, "아이디는 최소 4자 이상, 10자 이하의 알파벳 소문자(a~z), 숫자(0~9)이어야 합니다."),
    INVALID_PASSWORD(
            HttpStatus.BAD_REQUEST,
            "비밀번호는 최소 8자 이상, 15자 이하의 알파벳 대소문자(a~z, A~Z), 숫자(0~9), 특수문자이어야 합니다."),
    INVALID_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "전화번호는 9~15자리의 숫자와 '-'이어야 합니다."),

    // 403 Forbidden
    NOT_OWNER(HttpStatus.FORBIDDEN, "사장 권한이 없습니다."),
    NOT_MASTER(HttpStatus.FORBIDDEN, "마스터 권한이 없습니다."),

    // 404 Not Found
    NOT_EXIST_USER(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    NOT_EXIST_ADDRESS(HttpStatus.NOT_FOUND, "존재하지 않는 배송지입니다."),

    // 409 Conflict
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 사용중인 아이디입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용중인 닉네임입니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getName() {
        return this.name();
    }
}
