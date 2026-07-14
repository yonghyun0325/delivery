package com.delivery.domain.store.exception;

import com.delivery.global.exception.ErrorCode;
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
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "지역을 찾을 수 없습니다."),
    STORE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 가게에 대한 권한이 없습니다."),
    CATEGORY_IN_USE(HttpStatus.BAD_REQUEST, "해당 카테고리를 사용 중인 가게가 있습니다."),
    REGION_IN_USE(HttpStatus.BAD_REQUEST, "해당 지역을 사용 중인 가게가 있습니다."),
    INVALID_STORE_NAME(HttpStatus.BAD_REQUEST, "가게 이름은 1~50자 이내여야 합니다."),
    INVALID_ADDRESS(HttpStatus.BAD_REQUEST, "주소는 255자 이내여야 합니다."),
    INVALID_PHONE(HttpStatus.BAD_REQUEST, "전화번호는 20자 이내여야 합니다."),
    INVALID_DESCRIPTION(HttpStatus.BAD_REQUEST, "설명은 500자 이내여야 합니다."),
    INVALID_MIN_ORDER_AMOUNT(HttpStatus.BAD_REQUEST, "최소 주문 금액은 0 이상이어야 합니다."),
    INVALID_CATEGORY_NAME(HttpStatus.BAD_REQUEST, "카테고리 이름은 1~50자 이내여야 합니다."),
    INVALID_REGION_NAME(HttpStatus.BAD_REQUEST, "지역 이름은 1~100자 이내여야 합니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getName() {
        return this.name();
    }
}
