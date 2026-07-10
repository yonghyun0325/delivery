package com.delivery.domain.ai.client;

import com.delivery.domain.ai.dto.gemini.GeminiErrorResponse;
import com.delivery.domain.ai.dto.gemini.GeminiGenerateContentRequest;
import com.delivery.domain.ai.dto.gemini.GeminiGenerateContentResponse;
import com.delivery.domain.ai.dto.gemini.GeminiResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

// Gemini generateContent API를 호출하는 역할만 담당함 (infra 계층 성격).
// 글자수 검증, 프롬프트에 문구 삽입, 로그 저장 같은 비즈니스 로직은 여기 없음 -
// 그건 이 클래스를 호출하는 AiService의 책임.
@Component
public class GeminiClient {

    // Gemini가 응답을 안 주면 요청 스레드가 무한 대기하지 않도록 타임아웃을 둠.
    // 실제로 Gemini 응답이 16초 넘게 걸리는 경우가 있어(직접 curl로 확인, server-timing
    // dur=16098) read timeout을 30초로 넉넉하게 잡음 - 너무 타이트하면 정상 응답도
    // 타임아웃으로 오탐하게 됨.
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

    // RestClient: Spring Boot 3.2+에 내장된 동기 HTTP 클라이언트.
    // RestTemplate의 최신 대체제 - Builder로 baseUrl 등 공통 설정을 미리 잡아두고 재사용함.
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GeminiClient(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${gemini.base-url}") String baseUrl,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model) {
        // ClientHttpRequestFactoryBuilder.jdk()는 RestClient가 팩토리 미지정 시 쓰는
        // 기본 구현(JDK HttpClient)과 동일함 - SimpleClientHttpRequestFactory(구식
        // HttpURLConnection 기반)는 Gemini 응답의 Content-Type을 application/octet-stream으로
        // 잘못 처리하는 문제가 있어서 타임아웃만 얹은 이 방식으로 교체함.
        ClientHttpRequestFactory requestFactory =
                ClientHttpRequestFactoryBuilder.jdk()
                        .build(
                                ClientHttpRequestFactorySettings.defaults()
                                        .withConnectTimeout(CONNECT_TIMEOUT)
                                        .withReadTimeout(READ_TIMEOUT));

        this.restClient = restClientBuilder.baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    // prompt는 AiService이 글자수 검증 + "50자 이하로" 문구 삽입까지 끝낸 최종 텍스트.
    // 실패(4xx/5xx) 시 응답 본문을 파싱해 Gemini가 준 실제 사유(error.message)만 뽑아
    // GeminiResponseException으로 다시 던짐 - AiService가 잡아서 AI_GENERATION_FAILED로
    // 변환하고 실패 로그를 남기도록 함. 응답 자체가 없는 순수 네트워크 오류(타임아웃 등)는
    // 파싱할 본문이 없으므로 RestClientException 그대로 전파됨(AiService가 동일하게 처리).
    // API 키가 URL 쿼리에 들어가므로 요청/응답을 로깅하지 않음.
    public String generateContent(String prompt) {
        try {
            GeminiGenerateContentResponse response =
                    restClient
                            .post()
                            .uri("/v1beta/models/{model}:generateContent?key={key}", model, apiKey)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(GeminiGenerateContentRequest.ofText(prompt))
                            .retrieve()
                            .body(GeminiGenerateContentResponse.class);

            return response.firstText();
        } catch (RestClientResponseException e) {
            throw new GeminiResponseException(extractErrorMessage(e));
        }
    }

    private String extractErrorMessage(RestClientResponseException e) {
        try {
            GeminiErrorResponse errorResponse =
                    objectMapper.readValue(e.getResponseBodyAsString(), GeminiErrorResponse.class);
            // "404 NOT_FOUND: {message}" 형태 - HTTP 응답의 reason phrase는 HTTP/2에서
            // 아예 없을 수 있어(상태줄 자체가 없는 프로토콜) 대신 Gemini가 JSON으로 직접
            // 주는 code/status를 그대로 씀 - 프로토콜과 무관하게 항상 채워져 있음.
            GeminiErrorResponse.Error error = errorResponse.error();
            return error.code() + " " + error.status() + ": " + error.message();
        } catch (Exception parseFailure) {
            // Gemini가 항상 { "error": {...} } 포맷을 지킨다는 보장은 없으므로,
            // 파싱 자체가 실패하면 원래 예외 메시지로 안전하게 폴백.
            return e.getMessage();
        }
    }
}
