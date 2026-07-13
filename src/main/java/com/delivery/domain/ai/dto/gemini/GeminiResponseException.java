package com.delivery.domain.ai.dto.gemini;

// Gemini 응답 파싱 실패(예: 세이프티 필터로 candidates가 비어있는 경우) 신호용.
// BusinessException을 상속하지 않음 - HTTP 응답 코드로 직접 노출되는 게 아니라
// AiService가 잡아서 RestClientException과 동일하게 AI_GENERATION_FAILED로 변환함.
public class GeminiResponseException extends RuntimeException {

    public GeminiResponseException(String message) {
        super(message);
    }
}
