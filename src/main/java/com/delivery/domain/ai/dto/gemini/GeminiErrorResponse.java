package com.delivery.domain.ai.dto.gemini;

// Gemini(Google API 공통) 에러 응답 구조.
// { "error": { "code": 404, "message": "...", "status": "NOT_FOUND" } }
public record GeminiErrorResponse(Error error) {

    public record Error(int code, String message, String status) {}
}
