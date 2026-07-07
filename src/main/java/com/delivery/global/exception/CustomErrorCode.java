package com.delivery.global.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

/** ErrorCode 내용 변경용 객체 */
@AllArgsConstructor
public class CustomErrorCode implements ErrorCode {
    private final HttpStatus httpStatus;
    private final String name;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
