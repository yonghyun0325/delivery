package com.delivery.domain.reviewreply.exception;

import com.delivery.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReviewReplyErrorCode implements ErrorCode {

    // 400 Bad Request
    REVIEW_REPLY_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 답글이 등록된 리뷰입니다."),
    REVIEW_REPLY_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 답글입니다."),
    EMPTY_REPLY_CONTENT(HttpStatus.BAD_REQUEST, "답글 내용은 필수입니다."),

    // 403 Forbidden
    REVIEW_REPLY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 답글에 대한 권한이 없습니다."),

    // 404 Not Found
    REVIEW_REPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "답글을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getName() {
        return this.name();
    }
}
