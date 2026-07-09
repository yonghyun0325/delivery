package com.delivery.domain.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.delivery.domain.ai.client.GeminiClient;
import com.delivery.domain.ai.exception.AiErrorCode;
import com.delivery.domain.ai.exception.AiException;
import com.delivery.domain.ai.repository.AiLogRepository;
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

    @Mock private AiLogRepository aiLogRepository;

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
            verify(aiLogRepository)
                    .save(
                            argThat(
                                    log ->
                                            log.isSuccess()
                                                    && "맛있는 설명".equals(log.getResponseText())));
        }

        @Test
        @DisplayName("200자를 초과하면 AI_PROMPT_TOO_LONG 예외를 던지고 Gemini를 호출하지 않는다")
        void generateProductDescription_throwsWhenPromptTooLong() {
            String longPrompt = "a".repeat(201);

            assertThatExceptionOfType(AiException.class)
                    .isThrownBy(() -> aiService.generateProductDescription(longPrompt))
                    .extracting(BusinessException::getErrorCode)
                    .isEqualTo(AiErrorCode.AI_PROMPT_TOO_LONG);

            verifyNoInteractions(geminiClient, aiLogRepository);
        }

        @Test
        @DisplayName("Gemini 호출이 실패하면 AI_GENERATION_FAILED 예외를 던지고 실패 로그를 저장한다")
        void generateProductDescription_throwsAndLogsFailure_whenGeminiFails() {
            given(geminiClient.generateContent(any())).willThrow(new RestClientException("연결 실패"));

            assertThatExceptionOfType(AiException.class)
                    .isThrownBy(() -> aiService.generateProductDescription("김치찌개 설명 써줘"))
                    .extracting(BusinessException::getErrorCode)
                    .isEqualTo(AiErrorCode.AI_GENERATION_FAILED);

            verify(aiLogRepository)
                    .save(argThat(log -> !log.isSuccess() && log.getResponseText() == null));
        }
    }
}
