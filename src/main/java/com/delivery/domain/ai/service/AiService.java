package com.delivery.domain.ai.service;

import com.delivery.domain.ai.client.GeminiClient;
import com.delivery.domain.ai.entity.AiLogEntity;
import com.delivery.domain.ai.entity.AiRequestType;
import com.delivery.domain.ai.exception.AiErrorCode;
import com.delivery.domain.ai.exception.AiException;
import com.delivery.domain.ai.repository.AiLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class AiService {

    private static final int PROMPT_MAX_LENGTH = 200;
    private static final String CONCISE_SUFFIX = " 답변을 최대한 간결하게 50자 이하로 메뉴 설명을 작성해줘.";

    private final GeminiClient geminiClient;
    private final AiLogRepository aiLogRepository;

    public String generateProductDescription(String prompt) {
        if (prompt.length() > PROMPT_MAX_LENGTH) {
            throw new AiException(AiErrorCode.AI_PROMPT_TOO_LONG);
        }

        String finalPrompt = prompt + CONCISE_SUFFIX;

        try {
            String response = geminiClient.generateContent(finalPrompt);
            aiLogRepository.save(
                    new AiLogEntity(
                            AiRequestType.PRODUCT_DESCRIPTION, null, finalPrompt, response, true));
            return response;
        } catch (RestClientException e) {
            aiLogRepository.save(
                    new AiLogEntity(
                            AiRequestType.PRODUCT_DESCRIPTION, null, finalPrompt, null, false));
            throw new AiException(AiErrorCode.AI_GENERATION_FAILED);
        }
    }
}
