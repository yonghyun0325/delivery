package com.delivery.domain.ai.dto.gemini;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.delivery.domain.ai.dto.gemini.GeminiGenerateContentResponse.Candidate;
import com.delivery.domain.ai.dto.gemini.GeminiGenerateContentResponse.Content;
import com.delivery.domain.ai.dto.gemini.GeminiGenerateContentResponse.Part;
import com.delivery.domain.ai.dto.gemini.GeminiGenerateContentResponse.PromptFeedback;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GeminiGenerateContentResponseTest {

    @Nested
    @DisplayName("firstText")
    class FirstText {

        @Test
        @DisplayName("정상 응답이면 첫 번째 candidate의 text를 반환한다")
        void firstText_returnsText_whenValid() {
            GeminiGenerateContentResponse response =
                    new GeminiGenerateContentResponse(
                            List.of(new Candidate(new Content(List.of(new Part("맛있는 설명"))))), null);

            assertThat(response.firstText()).isEqualTo("맛있는 설명");
        }

        @Test
        @DisplayName("candidates가 비어있으면 GeminiResponseException을 던진다")
        void firstText_throws_whenCandidatesEmpty() {
            GeminiGenerateContentResponse response =
                    new GeminiGenerateContentResponse(List.of(), null);

            assertThatExceptionOfType(GeminiResponseException.class)
                    .isThrownBy(response::firstText);
        }

        @Test
        @DisplayName("candidates가 null이면 GeminiResponseException을 던진다")
        void firstText_throws_whenCandidatesNull() {
            GeminiGenerateContentResponse response = new GeminiGenerateContentResponse(null, null);

            assertThatExceptionOfType(GeminiResponseException.class)
                    .isThrownBy(response::firstText);
        }

        @Test
        @DisplayName("content가 null이면 GeminiResponseException을 던진다")
        void firstText_throws_whenContentNull() {
            GeminiGenerateContentResponse response =
                    new GeminiGenerateContentResponse(List.of(new Candidate(null)), null);

            assertThatExceptionOfType(GeminiResponseException.class)
                    .isThrownBy(response::firstText);
        }

        @Test
        @DisplayName("parts가 비어있으면 GeminiResponseException을 던진다")
        void firstText_throws_whenPartsEmpty() {
            GeminiGenerateContentResponse response =
                    new GeminiGenerateContentResponse(
                            List.of(new Candidate(new Content(List.of()))), null);

            assertThatExceptionOfType(GeminiResponseException.class)
                    .isThrownBy(response::firstText);
        }

        @Test
        @DisplayName("candidates가 비어있고 promptFeedback.blockReason이 있으면 메시지에 포함한다")
        void firstText_includesBlockReason_whenCandidatesBlockedBySafety() {
            GeminiGenerateContentResponse response =
                    new GeminiGenerateContentResponse(List.of(), new PromptFeedback("SAFETY"));

            assertThatExceptionOfType(GeminiResponseException.class)
                    .isThrownBy(response::firstText)
                    .withMessageContaining("SAFETY");
        }
    }
}
