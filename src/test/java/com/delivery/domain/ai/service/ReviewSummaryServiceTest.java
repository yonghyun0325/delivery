package com.delivery.domain.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.delivery.domain.ai.dto.response.ReviewSummaryResponse;
import com.delivery.domain.ai.dto.response.ReviewSummaryStatus;
import com.delivery.domain.ai.entity.StoreReviewSummaryEntity;
import com.delivery.domain.ai.repository.StoreReviewSummaryRepository;
import com.delivery.domain.review.service.ReviewService;
import com.delivery.domain.store.exception.StoreErrorCode;
import com.delivery.domain.store.exception.StoreException;
import com.delivery.domain.store.service.StoreService;
import com.delivery.global.exception.BusinessException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewSummaryServiceTest {

    @Mock private ReviewService reviewService;

    @Mock private AiService aiService;

    @Mock private StoreService storeService;

    @Mock private StoreReviewSummaryRepository storeReviewSummaryRepository;

    @InjectMocks private ReviewSummaryService reviewSummaryService;

    @Nested
    @DisplayName("리뷰 요약 조회")
    class GetSummary {

        @Test
        @DisplayName("리뷰가 10개 미만이면 NOT_ENOUGH_REVIEWS를 반환한다")
        void getSummary_returnsNotEnoughReviews_whenReviewCountBelowThreshold() {
            UUID storeId = UUID.randomUUID();
            given(reviewService.countReviewsByStore(storeId)).willReturn(9L);

            ReviewSummaryResponse response = reviewSummaryService.getSummary(storeId);

            assertThat(response.status()).isEqualTo(ReviewSummaryStatus.NOT_ENOUGH_REVIEWS);
            assertThat(response.reviewCount()).isEqualTo(9L);
            assertThat(response.summary()).isNull();
        }

        @Test
        @DisplayName("존재하지 않거나 삭제된 가게면 STORE_NOT_FOUND 예외를 던진다")
        void getSummary_throwsStoreNotFound_whenStoreMissing() {
            UUID storeId = UUID.randomUUID();
            given(storeService.getStore(storeId))
                    .willThrow(new StoreException(StoreErrorCode.STORE_NOT_FOUND));

            assertThatExceptionOfType(StoreException.class)
                    .isThrownBy(() -> reviewSummaryService.getSummary(storeId))
                    .extracting(BusinessException::getErrorCode)
                    .isEqualTo(StoreErrorCode.STORE_NOT_FOUND);

            verify(reviewService, never()).countReviewsByStore(any());
        }

        @Test
        @DisplayName("리뷰가 10개 이상이지만 아직 캐시가 없으면 PENDING_GENERATION을 반환한다")
        void getSummary_returnsPendingGeneration_whenNoCacheYet() {
            UUID storeId = UUID.randomUUID();
            given(reviewService.countReviewsByStore(storeId)).willReturn(10L);
            given(storeReviewSummaryRepository.findById(storeId)).willReturn(Optional.empty());

            ReviewSummaryResponse response = reviewSummaryService.getSummary(storeId);

            assertThat(response.status()).isEqualTo(ReviewSummaryStatus.PENDING_GENERATION);
            assertThat(response.reviewCount()).isEqualTo(10L);
        }

        @Test
        @DisplayName("캐시된 요약이 있으면 READY와 함께 요약 내용을 반환한다")
        void getSummary_returnsReady_whenCacheExists() {
            UUID storeId = UUID.randomUUID();
            StoreReviewSummaryEntity entity =
                    StoreReviewSummaryEntity.create(storeId, "요약된 리뷰 내용입니다.", 10L);
            given(reviewService.countReviewsByStore(storeId)).willReturn(12L);
            given(storeReviewSummaryRepository.findById(storeId)).willReturn(Optional.of(entity));

            ReviewSummaryResponse response = reviewSummaryService.getSummary(storeId);

            assertThat(response.status()).isEqualTo(ReviewSummaryStatus.READY);
            assertThat(response.summary()).isEqualTo("요약된 리뷰 내용입니다.");
            // 이후 리뷰가 12개로 늘었어도(라이브 카운트), 요약은 생성 시점 개수(10)를 기준으로
            // 만들어졌으므로 reviewCount는 라이브 값이 아니라 생성 시점 값을 반영해야 한다
            assertThat(response.reviewCount()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("리뷰 요약 재생성")
    class RegenerateIfStale {

        @Test
        @DisplayName("캐시가 없으면 Gemini를 호출해 요약을 새로 생성/저장한다")
        void regenerateIfStale_createsSummary_whenNoExistingCache() {
            UUID storeId = UUID.randomUUID();
            given(storeService.existsActiveStore(storeId)).willReturn(true);
            given(reviewService.countReviewsByStore(storeId)).willReturn(10L);
            given(storeReviewSummaryRepository.findById(storeId)).willReturn(Optional.empty());
            given(reviewService.getReviewContentsByStore(storeId))
                    .willReturn(List.of("맛있어요", "친절해요"));
            given(aiService.summarizeStoreReviews(eq(storeId), anyList())).willReturn("요약본");

            reviewSummaryService.regenerateIfStale(storeId);

            verify(aiService).summarizeStoreReviews(eq(storeId), anyList());
            verify(storeReviewSummaryRepository).save(any(StoreReviewSummaryEntity.class));
        }

        @Test
        @DisplayName("마지막 생성 이후 새 리뷰가 없으면 Gemini를 호출하지 않고 스킵한다")
        void regenerateIfStale_skips_whenReviewCountUnchanged() {
            UUID storeId = UUID.randomUUID();
            StoreReviewSummaryEntity existing =
                    StoreReviewSummaryEntity.create(storeId, "기존 요약", 10L);
            given(storeService.existsActiveStore(storeId)).willReturn(true);
            given(reviewService.countReviewsByStore(storeId)).willReturn(10L);
            given(storeReviewSummaryRepository.findById(storeId)).willReturn(Optional.of(existing));

            reviewSummaryService.regenerateIfStale(storeId);

            verify(aiService, never()).summarizeStoreReviews(any(), anyList());
            verify(storeReviewSummaryRepository, never()).save(any());
        }

        @Test
        @DisplayName("새 리뷰가 늘었으면 기존 캐시를 갱신한다")
        void regenerateIfStale_updatesExisting_whenReviewCountIncreased() {
            UUID storeId = UUID.randomUUID();
            StoreReviewSummaryEntity existing =
                    StoreReviewSummaryEntity.create(storeId, "기존 요약", 10L);
            given(storeService.existsActiveStore(storeId)).willReturn(true);
            given(reviewService.countReviewsByStore(storeId)).willReturn(11L);
            given(storeReviewSummaryRepository.findById(storeId)).willReturn(Optional.of(existing));
            given(reviewService.getReviewContentsByStore(storeId))
                    .willReturn(List.of("맛있어요", "친절해요", "또 시킬게요"));
            given(aiService.summarizeStoreReviews(eq(storeId), anyList())).willReturn("갱신된 요약");

            reviewSummaryService.regenerateIfStale(storeId);

            assertThat(existing.getSummaryText()).isEqualTo("갱신된 요약");
            assertThat(existing.getReviewCountAtGeneration()).isEqualTo(11L);
            verify(storeReviewSummaryRepository).save(existing);
        }

        @Test
        @DisplayName("삭제된 가게면 Gemini 호출 없이 스킵한다")
        void regenerateIfStale_skips_whenStoreDeleted() {
            UUID storeId = UUID.randomUUID();
            given(storeService.existsActiveStore(storeId)).willReturn(false);

            reviewSummaryService.regenerateIfStale(storeId);

            verify(reviewService, never()).countReviewsByStore(any());
            verify(aiService, never()).summarizeStoreReviews(any(), anyList());
            verify(storeReviewSummaryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("요약 대상 가게 목록 조회")
    class FindTargetStoreIds {

        @Test
        @DisplayName("삭제된 가게는 대상 목록에서 제외한다")
        void findTargetStoreIds_excludesDeletedStores() {
            UUID activeStoreId = UUID.randomUUID();
            UUID deletedStoreId = UUID.randomUUID();
            given(reviewService.findStoreIdsWithReviewCountAtLeast(10))
                    .willReturn(List.of(activeStoreId, deletedStoreId));
            given(storeService.existsActiveStore(activeStoreId)).willReturn(true);
            given(storeService.existsActiveStore(deletedStoreId)).willReturn(false);

            List<UUID> targetStoreIds = reviewSummaryService.findTargetStoreIds();

            assertThat(targetStoreIds).containsExactly(activeStoreId);
        }
    }
}
