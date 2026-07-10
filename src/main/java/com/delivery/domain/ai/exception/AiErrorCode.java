package com.delivery.domain.ai.exception;

import com.delivery.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AiErrorCode implements ErrorCode {
    // 400 Bad Request
    AI_PROMPT_REQUIRED(HttpStatus.BAD_REQUEST, "AI 설명 생성 시 프롬프트는 필수입니다."),
    AI_PROMPT_TOO_LONG(HttpStatus.BAD_REQUEST, "프롬프트는 200자 이하로 입력해주세요."),

    // 502 Bad Gateway
    AI_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, "AI 설명 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getName() {
        return this.name();
    }
}
