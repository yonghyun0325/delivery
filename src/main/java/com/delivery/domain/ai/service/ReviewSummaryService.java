package com.delivery.domain.ai.service;

import com.delivery.domain.ai.dto.response.ReviewSummaryResponse;
import com.delivery.domain.ai.entity.StoreReviewSummaryEntity;
import com.delivery.domain.ai.repository.StoreReviewSummaryRepository;
import com.delivery.domain.review.service.ReviewService;
import com.delivery.domain.store.service.StoreService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 가게별 AI 리뷰 요약의 조회(컨트롤러용)와 재생성(스케줄러용)을 담당.
// 실제 Gemini 호출/로그는 AiService에 위임하고, 여기선 "리뷰 10개 이상인지",
// "마지막 생성 이후 새 리뷰가 있는지" 같은 요약 캐시 자체의 정책만 다룬다.
@Service
@RequiredArgsConstructor
public class ReviewSummaryService {

    private static final long REVIEW_COUNT_THRESHOLD = 10;

    private final ReviewService reviewService;
    private final AiService aiService;
    private final StoreService storeService;
    private final StoreReviewSummaryRepository storeReviewSummaryRepository;

    @Transactional(readOnly = true)
    public ReviewSummaryResponse getSummary(UUID storeId) {
        // 존재하지 않거나 삭제된 storeId면 다른 Store API와 동일하게 STORE_NOT_FOUND로 404
        storeService.getStore(storeId);

        long reviewCount = reviewService.countReviewsByStore(storeId);

        if (reviewCount < REVIEW_COUNT_THRESHOLD) {
            return ReviewSummaryResponse.notEnoughReviews(reviewCount);
        }

        return storeReviewSummaryRepository
                .findById(storeId)
                .map(ReviewSummaryResponse::ready)
                .orElseGet(() -> ReviewSummaryResponse.pendingGeneration(reviewCount));
    }

    // 삭제된 가게는 대상에서 제외 - 리뷰 자체는 고객이 나중에 볼 수 있어 지우지 않지만,
    // 더 이상 존재하지 않는 가게를 요약하느라 Gemini 호출을 낭비하지 않는다.
    public List<UUID> findTargetStoreIds() {
        return reviewService.findStoreIdsWithReviewCountAtLeast(REVIEW_COUNT_THRESHOLD).stream()
                .filter(storeService::existsActiveStore)
                .toList();
    }

    // 새 리뷰가 없으면(현재 개수 == 마지막 생성 시점 개수) Gemini를 호출하지 않고 스킵한다.
    public void regenerateIfStale(UUID storeId) {
        if (!storeService.existsActiveStore(storeId)) {
            return;
        }

        long currentCount = reviewService.countReviewsByStore(storeId);

        StoreReviewSummaryEntity existing =
                storeReviewSummaryRepository.findById(storeId).orElse(null);
        if (existing != null && existing.getReviewCountAtGeneration() == currentCount) {
            return;
        }

        List<String> reviewContents = reviewService.getReviewContentsByStore(storeId);
        String summary = aiService.summarizeStoreReviews(storeId, reviewContents);

        if (existing != null) {
            existing.updateSummary(summary, currentCount);
            storeReviewSummaryRepository.save(existing);
        } else {
            storeReviewSummaryRepository.save(
                    StoreReviewSummaryEntity.create(storeId, summary, currentCount));
        }
    }
}
