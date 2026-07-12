package com.delivery.domain.review.exception;

import com.delivery.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    // 400 Bad Request
    INVALID_RATING(HttpStatus.BAD_REQUEST, "평점은 1점 이상 5점 이하여야 합니다."),
    EMPTY_CONTENT(HttpStatus.BAD_REQUEST, "리뷰 내용은 필수입니다."),
    REVIEW_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 리뷰입니다."),

    // 403 Forbidden
    REVIEW_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 리뷰에 대한 권한이 없습니다."),

    // 404 Not Found
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getName() {
        return this.name();
    }
}
