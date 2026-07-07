package com.delivery.domain.menu.exception;

import com.delivery.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MenuErrorCode implements ErrorCode {
    // 400 Bad Request
    INVALID_MENU_NAME(HttpStatus.BAD_REQUEST, "메뉴 이름은 1자 이상 100자 이하로 입력해주세요."),
    INVALID_MENU_PRICE(HttpStatus.BAD_REQUEST, "가격은 0보다 큰 값이어야 합니다."),
    AI_PROMPT_REQUIRED(HttpStatus.BAD_REQUEST, "AI 설명 생성 시 프롬프트는 필수입니다."),
    AI_PROMPT_TOO_LONG(HttpStatus.BAD_REQUEST, "프롬프트는 200자 이하로 입력해주세요."),
    MENU_HIDDEN_STATUS_REQUIRED(HttpStatus.BAD_REQUEST, "숨김 여부 값은 필수입니다."),

    // 403 Forbidden
    NOT_MENU_STORE_OWNER(HttpStatus.FORBIDDEN, "해당 가게의 메뉴에 대한 권한이 없습니다."),

    // 404 Not Found
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."),
    MENU_STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다."),

    // 502 Bad Gateway
    AI_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, "AI 설명 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getName() {
        return this.name();
    }
}
