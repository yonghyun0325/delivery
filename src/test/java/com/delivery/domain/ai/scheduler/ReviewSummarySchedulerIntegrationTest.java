package com.delivery.domain.ai.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.delivery.config.AbstractIntegrationTest;
import com.delivery.domain.ai.client.GeminiClient;
import com.delivery.domain.ai.entity.StoreReviewSummaryEntity;
import com.delivery.domain.ai.repository.StoreReviewSummaryRepository;
import com.delivery.domain.menu.fixture.StoreTestFixture;
import com.delivery.domain.review.entity.Review;
import com.delivery.domain.review.repository.ReviewRepository;
import com.delivery.domain.store.entity.Store;
import com.delivery.domain.store.repository.StoreRepository;
import com.delivery.global.cache.WithdrawnUserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

// 이 세션 내내 수동(curl + 임시 테스트)으로만 검증했던 리뷰 요약 배치 흐름을 자동화된
// 테스트로 고정한다. GeminiClient만 목으로 막고 나머지(스케줄러, 서비스, 실제 DB)는
// 전부 진짜로 띄운다.
// @Transactional로 테스트가 만든 Store/Review 데이터를 종료 시 롤백 - 공유
// Testcontainers DB에 다른 도메인 테스트와 데이터가 남아 충돌하는 것을 방지한다.
@SpringBootTest(
        properties = {
            "gemini.api-key=test-dummy-key",
            "gemini.base-url=https://generativelanguage.googleapis.com",
            "gemini.model=gemini-1.5-flash"
        })
@Transactional
class ReviewSummarySchedulerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private ReviewSummaryScheduler scheduler;
    @Autowired private StoreRepository storeRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private StoreReviewSummaryRepository storeReviewSummaryRepository;
    @Autowired private WithdrawnUserRepository withdrawnUserRepository;

    @MockitoBean private GeminiClient geminiClient;

    private UUID createTestStore(Long ownerId) {
        Store store = StoreTestFixture.DEFAULT.createStore(ownerId);
        return storeRepository.save(store).getStoreId();
    }

    private void createReviews(UUID storeId, int count) {
        for (int i = 0; i < count; i++) {
            reviewRepository.save(
                    Review.create(UUID.randomUUID(), (long) i, storeId, 5, "맛있어요 " + i));
        }
    }

    @Test
    @DisplayName("리뷰가 10개 이상인 가게는 요약이 생성되고, 새 리뷰 없이 재실행하면 스킵된다")
    void regenerate_createsSummary_thenSkipsWhenUnchanged() {
        UUID storeId = createTestStore(700001L);
        createReviews(storeId, 10);
        given(geminiClient.generateContentForBatch(any())).willReturn("첫 요약본입니다.");

        scheduler.regenerateStoreReviewSummaries();

        Optional<StoreReviewSummaryEntity> first = storeReviewSummaryRepository.findById(storeId);
        assertThat(first).isPresent();
        assertThat(first.get().getSummaryText()).isEqualTo("첫 요약본입니다.");
        assertThat(first.get().getReviewCountAtGeneration()).isEqualTo(10L);
        var firstGeneratedAt = first.get().getGeneratedAt();

        // 새 리뷰 없이 다시 실행 - Gemini를 다시 호출하면 안 됨
        scheduler.regenerateStoreReviewSummaries();

        verify(geminiClient, times(1)).generateContentForBatch(any());
        StoreReviewSummaryEntity second = storeReviewSummaryRepository.findById(storeId).get();
        assertThat(second.getGeneratedAt()).isEqualTo(firstGeneratedAt);
    }

    @Test
    @DisplayName("새 리뷰가 추가되면 요약이 재생성된다")
    void regenerate_regeneratesSummary_whenNewReviewAdded() {
        UUID storeId = createTestStore(700002L);
        createReviews(storeId, 10);
        given(geminiClient.generateContentForBatch(any()))
                .willReturn("첫 요약본입니다.")
                .willReturn("갱신된 요약본입니다.");

        scheduler.regenerateStoreReviewSummaries();
        createReviews(storeId, 1);
        scheduler.regenerateStoreReviewSummaries();

        StoreReviewSummaryEntity updated = storeReviewSummaryRepository.findById(storeId).get();
        assertThat(updated.getSummaryText()).isEqualTo("갱신된 요약본입니다.");
        assertThat(updated.getReviewCountAtGeneration()).isEqualTo(11L);
        verify(geminiClient, times(2)).generateContentForBatch(any());
    }

    @Test
    @DisplayName("리뷰가 10개 미만인 가게는 대상에서 제외되어 Gemini를 호출하지 않는다")
    void regenerate_skips_whenBelowThreshold() {
        UUID storeId = createTestStore(700003L);
        createReviews(storeId, 9);

        scheduler.regenerateStoreReviewSummaries();

        assertThat(storeReviewSummaryRepository.findById(storeId)).isEmpty();
        verify(geminiClient, never()).generateContentForBatch(any());
    }

    @Test
    @DisplayName("삭제된 가게는 리뷰가 10개 이상이어도 대상에서 제외된다")
    void regenerate_skips_whenStoreDeleted() {
        UUID storeId = createTestStore(700004L);
        createReviews(storeId, 10);
        Store store = storeRepository.findByStoreIdAndDeletedAtIsNull(storeId).orElseThrow();
        store.delete("SYSTEM");
        storeRepository.saveAndFlush(store);

        scheduler.regenerateStoreReviewSummaries();

        assertThat(storeReviewSummaryRepository.findById(storeId)).isEmpty();
        verify(geminiClient, never()).generateContentForBatch(any());
    }
}
