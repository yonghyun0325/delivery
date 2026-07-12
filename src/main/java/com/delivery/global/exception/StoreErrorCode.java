package com.delivery.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StoreErrorCode implements ErrorCode {
    DUPLICATE_STORE(HttpStatus.BAD_REQUEST, "이미 등록된 가게입니다."),
    DUPLICATE_CATEGORY(HttpStatus.BAD_REQUEST, "이미 등록된 카테고리입니다."),
    DUPLICATE_REGION(HttpStatus.BAD_REQUEST, "이미 등록된 지역입니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "지역을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getName() {
        return this.name();
    }
}
