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
    INVALID_NICKNAME(
            HttpStatus.BAD_REQUEST,
            "닉네임은 최소 2자 이상, 16자 이하의 알파벳 대소문자(a~z, A~Z), 숫자(0~9), 한글이어야 합니다."),
    INVALID_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "전화번호는 11자리의 숫자이어야 합니다."),
    EXCEED_MAX_ADDRESS(HttpStatus.BAD_REQUEST, "배송지는 최대 10개까지 등록할 수 있습니다."),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "존재하지 않는 권한 입니다."),
    INVALID_USER_STATUS(HttpStatus.BAD_REQUEST, "존재하지 유저 상태 입니다."),

    // 404 Not Found
    NOT_EXIST_USER(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    NOT_EXIST_ADDRESS(HttpStatus.NOT_FOUND, "존재하지 않는 배송지입니다."),

    // 409 Conflict
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 사용중인 아이디입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용중인 닉네임입니다."),
    ALREADY_EXISTS_DEFAULT_ADDRESS(HttpStatus.CONFLICT, "이미 기본 배송지가 설정되어 있습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getName() {
        return this.name();
    }
}
