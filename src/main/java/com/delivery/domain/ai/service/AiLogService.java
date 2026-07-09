package com.delivery.domain.ai.service;

import com.delivery.domain.ai.entity.AiLogEntity;
import com.delivery.domain.ai.entity.AiRequestType;
import com.delivery.domain.ai.repository.AiLogRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// AI 요청/응답 로그는 호출한 쪽(예: 메뉴 등록)의 트랜잭션 성패와 무관하게 항상 남아야 함 -
// REQUIRES_NEW로 별도 트랜잭션에 즉시 커밋해서, 이후 호출자 트랜잭션이 롤백되어도 로그는 유지됨.
@Service
@RequiredArgsConstructor
public class AiLogService {

    private final AiLogRepository aiLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(
            AiRequestType requestType,
            UUID referenceId,
            String requestText,
            String responseText,
            boolean success,
            String errorMessage) {
        aiLogRepository.save(
                new AiLogEntity(
                        requestType,
                        referenceId,
                        requestText,
                        responseText,
                        success,
                        errorMessage));
    }
}
