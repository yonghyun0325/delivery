package com.delivery.domain.ai.service;

import com.delivery.domain.ai.client.GeminiClient;
import com.delivery.domain.ai.dto.gemini.GeminiResponseException;
import com.delivery.domain.ai.entity.AiRequestType;
import com.delivery.domain.ai.exception.AiErrorCode;
import com.delivery.domain.ai.exception.AiException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private static final int PROMPT_MAX_LENGTH = 200;
    private static final String CONCISE_SUFFIX = " 답변을 최대한 간결하게 50자 이하로 메뉴 설명을 작성해줘.";
    private static final String REVIEW_SUMMARY_PREFIX =
            "다음은 한 가게에 대한 고객 리뷰 목록이다. 공통적으로 언급되는 맛, 서비스, 특징을 중심으로" + " 3문장 이내로 요약해줘.\n\n";

    private final GeminiClient geminiClient;
    private final AiLogService aiLogService;

    public String generateProductDescription(String prompt) {
        if (prompt.length() > PROMPT_MAX_LENGTH) {
            throw new AiException(AiErrorCode.AI_PROMPT_TOO_LONG);
        }

        String finalPrompt = prompt + CONCISE_SUFFIX;

        try {
            String response = geminiClient.generateContent(finalPrompt);
            aiLogService.saveLog(
                    AiRequestType.PRODUCT_DESCRIPTION, null, finalPrompt, response, true, null);
            return response;
        } catch (RestClientException | GeminiResponseException e) {
            log.error("Gemini 호출 실패: {}", e.getMessage(), e);
            aiLogService.saveLog(
                    AiRequestType.PRODUCT_DESCRIPTION,
                    null,
                    finalPrompt,
                    null,
                    false,
                    e.getMessage());
            throw new AiException(AiErrorCode.AI_GENERATION_FAILED);
        }
    }

    // storeId는 로그 조회/연관용 referenceId - 실제 요약 대상 리뷰 개수/재생성 여부 판단은
    // 호출자(ReviewSummaryService)의 책임이고, 여기선 순수하게 Gemini 호출 + 로그 저장만 한다.
    public String summarizeStoreReviews(UUID storeId, List<String> reviewContents) {
        String prompt =
                REVIEW_SUMMARY_PREFIX
                        + reviewContents.stream()
                                .map(content -> "- " + content)
                                .collect(Collectors.joining("\n"));

        try {
            String response = geminiClient.generateContentForBatch(prompt);
            aiLogService.saveLog(
                    AiRequestType.REVIEW_SUMMARY, storeId, prompt, response, true, null);
            return response;
        } catch (RestClientException | GeminiResponseException e) {
            log.error("Gemini 호출 실패: {}", e.getMessage(), e);
            aiLogService.saveLog(
                    AiRequestType.REVIEW_SUMMARY, storeId, prompt, null, false, e.getMessage());
            throw new AiException(AiErrorCode.AI_GENERATION_FAILED);
        }
    }
}
