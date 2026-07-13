package com.delivery.domain.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.delivery.domain.ai.client.GeminiClient;
import com.delivery.domain.ai.dto.gemini.GeminiResponseException;
import com.delivery.domain.ai.entity.AiRequestType;
import com.delivery.domain.ai.exception.AiErrorCode;
import com.delivery.domain.ai.exception.AiException;
import com.delivery.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock private GeminiClient geminiClient;

    @Mock private AiLogService aiLogService;

    @InjectMocks private AiService aiService;

    @Nested
    @DisplayName("상품 설명 생성")
    class GenerateProductDescription {

        @Test
        @DisplayName("성공하면 응답을 반환하고 성공 로그를 저장한다")
        void generateProductDescription_returnsResponse_andLogsSuccess() {
            given(geminiClient.generateContent(any())).willReturn("맛있는 설명");

            String result = aiService.generateProductDescription("김치찌개 설명 써줘");

            assertThat(result).isEqualTo("맛있는 설명");
            verify(aiLogService)
                    .saveLog(
                            eq(AiRequestType.PRODUCT_DESCRIPTION),
                            isNull(),
                            any(),
                            eq("맛있는 설명"),
                            eq(true),
                            isNull());
        }

        @Test
        @DisplayName("200자를 초과하면 AI_PROMPT_TOO_LONG 예외를 던지고 Gemini를 호출하지 않는다")
        void generateProductDescription_throwsWhenPromptTooLong() {
            String longPrompt = "a".repeat(201);

            assertThatExceptionOfType(AiException.class)
                    .isThrownBy(() -> aiService.generateProductDescription(longPrompt))
                    .extracting(BusinessException::getErrorCode)
                    .isEqualTo(AiErrorCode.AI_PROMPT_TOO_LONG);

            verifyNoInteractions(geminiClient, aiLogService);
        }

        @Test
        @DisplayName("Gemini 호출이 실패하면 AI_GENERATION_FAILED 예외를 던지고 실패 로그를 저장한다")
        void generateProductDescription_throwsAndLogsFailure_whenGeminiFails() {
            given(geminiClient.generateContent(any())).willThrow(new RestClientException("연결 실패"));

            assertThatExceptionOfType(AiException.class)
                    .isThrownBy(() -> aiService.generateProductDescription("김치찌개 설명 써줘"))
                    .extracting(BusinessException::getErrorCode)
                    .isEqualTo(AiErrorCode.AI_GENERATION_FAILED);

            verify(aiLogService)
                    .saveLog(
                            eq(AiRequestType.PRODUCT_DESCRIPTION),
                            isNull(),
                            any(),
                            isNull(),
                            eq(false),
                            eq("연결 실패"));
        }

        @Test
        @DisplayName(
                "Gemini가 세이프티 필터 등으로 빈 candidates를 반환하면 AI_GENERATION_FAILED 예외를 던지고 실패 로그를 저장한다")
        void generateProductDescription_throwsAndLogsFailure_whenGeminiReturnsEmptyCandidates() {
            given(geminiClient.generateContent(any()))
                    .willThrow(new GeminiResponseException("Gemini 응답에 candidates가 없습니다"));

            assertThatExceptionOfType(AiException.class)
                    .isThrownBy(() -> aiService.generateProductDescription("김치찌개 설명 써줘"))
                    .extracting(BusinessException::getErrorCode)
                    .isEqualTo(AiErrorCode.AI_GENERATION_FAILED);

            verify(aiLogService)
                    .saveLog(
                            eq(AiRequestType.PRODUCT_DESCRIPTION),
                            isNull(),
                            any(),
                            isNull(),
                            eq(false),
                            eq("Gemini 응답에 candidates가 없습니다"));
        }
    }
}
