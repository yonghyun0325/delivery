package com.delivery.domain.ai.dto.gemini;

import java.util.List;

// Gemini 응답 JSON의 최상위 구조를 표현하는 record.
// { "candidates": [ { "content": { "parts": [ { "text": "..." } ] } } ] }
// 실제 응답엔 usageMetadata, finishReason 같은 필드도 더 있지만, 우리가 안 쓰는 값이라
// 아예 필드를 안 만들었음 - Jackson은 DTO에 없는 필드를 만나면 에러 없이 그냥 무시함
// (Spring Boot 기본 설정: FAIL_ON_UNKNOWN_PROPERTIES = false).
public record GeminiGenerateContentResponse(List<Candidate> candidates) {

    // 요청 DTO와 대칭되는 중첩 구조. candidates는 응답 후보 배열인데(A/B 여러 개 나올 수 있음),
    // 우리는 늘 첫 번째 것만 사용함.
    public record Candidate(Content content) {}

    public record Content(List<Part> parts) {}

    public record Part(String text) {}

    // candidates[0].content.parts[0].text 를 한 번에 꺼내주는 헬퍼.
    // 이 메서드 덕분에 GeminiClient는 JSON이 몇 겹으로 감싸져 있는지 몰라도 됨.
    // record의 필드 접근자는 getXxx()가 아니라 필드명 그대로 메서드(candidates())로 생성됨.
    public String firstText() {
        return candidates().get(0).content().parts().get(0).text();
    }
}
