package com.delivery.domain.ai.dto.gemini;

import java.util.List;

// record: 필드를 선언하면 생성자/getter(필드명과 같은 메서드)/equals/hashCode/toString을
// 자동으로 만들어주는 불변(immutable) 데이터 클래스. 값 자체가 전부인 DTO에 적합함.
// 이 record는 Gemini generateContent 요청 JSON의 최상위 구조를 표현함:
// { "contents": [ { "parts": [ { "text": "..." } ] } ] }
public record GeminiGenerateContentRequest(List<Content> contents) {

    // record 안에 record를 중첩(nested)시켜서 JSON의 계단식 구조(contents -> parts -> text)를
    // 그대로 반영함. 바깥에서 따로 쓸 일 없는 타입들이라 별도 파일로 안 뺌.
    public record Content(List<Part> parts) {}

    public record Part(String text) {}

    // 우리 유스케이스는 항상 "텍스트 하나짜리 단일 프롬프트"만 보내므로,
    // GeminiClient가 new GeminiGenerateContentRequest(List.of(new Content(...)))처럼
    // 중첩 생성자를 직접 조립하지 않도록 감싸주는 편의 메서드.
    public static GeminiGenerateContentRequest ofText(String text) {
        return new GeminiGenerateContentRequest(List.of(new Content(List.of(new Part(text)))));
    }
}
