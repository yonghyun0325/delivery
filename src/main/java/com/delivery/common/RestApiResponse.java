package com.delivery.common;

import org.springframework.http.HttpStatus;

/** API 래퍼 */
public record RestApiResponse<T>(boolean success, int code, String message, T data, String error) {
    public static <T> RestApiResponse<T> success(HttpStatus httpStatus, String message, T data) {
        return new RestApiResponse<>(true, httpStatus.value(), message, data, null);
    }

    public static <T> RestApiResponse<T> fail(HttpStatus httpStatus, String message, String error) {
        return new RestApiResponse<>(false, httpStatus.value(), message, null, error);
    }
}
