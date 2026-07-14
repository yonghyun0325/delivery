package com.delivery.domain.ai.dto.response;

import com.delivery.domain.ai.entity.StoreReviewSummaryEntity;
import java.time.LocalDateTime;

public record ReviewSummaryResponse(
        ReviewSummaryStatus status, String summary, LocalDateTime generatedAt, long reviewCount) {

    public static ReviewSummaryResponse notEnoughReviews(long reviewCount) {
        return new ReviewSummaryResponse(
                ReviewSummaryStatus.NOT_ENOUGH_REVIEWS, null, null, reviewCount);
    }

    public static ReviewSummaryResponse pendingGeneration(long reviewCount) {
        return new ReviewSummaryResponse(
                ReviewSummaryStatus.PENDING_GENERATION, null, null, reviewCount);
    }

    // reviewCount는 현재 라이브 리뷰 개수가 아니라 이 요약이 실제로 반영한(생성 시점) 개수 -
    // 그 이후 새 리뷰가 더 쌓였어도 다음 배치 전까지는 이 요약에 포함되지 않았으므로 구분한다.
    public static ReviewSummaryResponse ready(StoreReviewSummaryEntity entity) {
        return new ReviewSummaryResponse(
                ReviewSummaryStatus.READY,
                entity.getSummaryText(),
                entity.getGeneratedAt(),
                entity.getReviewCountAtGeneration());
    }
}
