package com.delivery.domain.ReviewReply.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.delivery.domain.review.entity.Review;
import com.delivery.domain.review.repository.ReviewRepository;
import com.delivery.domain.reviewreply.dto.request.ReviewReplyRequest;
import com.delivery.domain.reviewreply.dto.response.ReviewReplyResponse;
import com.delivery.domain.reviewreply.entity.ReviewReply;
import com.delivery.domain.reviewreply.exception.ReviewReplyErrorCode;
import com.delivery.domain.reviewreply.exception.ReviewReplyException;
import com.delivery.domain.reviewreply.repository.ReviewReplyRepository;
import com.delivery.domain.reviewreply.service.ReviewReplyService;
import com.delivery.domain.store.entity.Store;
import com.delivery.domain.store.repository.StoreRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReviewReplyServiceTest {

    @Mock private ReviewRepository reviewRepository;

    @Mock private ReviewReplyRepository reviewReplyRepository;

    @Mock private StoreRepository storeRepository;

    @InjectMocks private ReviewReplyService reviewReplyService;

    private Long ownerId;
    private Long customerId;
    private UUID reviewId;
    private UUID storeId;
    private Review review;
    private Store store;

    @BeforeEach
    void setUp() {

        ownerId = 1L;
        customerId = 2L;
        reviewId = UUID.randomUUID();
        storeId = UUID.randomUUID();

        // 답글이 작성될 리뷰 생성
        review = Review.create(UUID.randomUUID(), customerId, storeId, 5, "정말 맛있었습니다.");

        // 테스트에서 리뷰 ID 직접 설정
        ReflectionTestUtils.setField(review, "id", reviewId);

        // 가게 소유자 검증에 필요한 Store mock 생성
        store = mock(Store.class);
    }

    @Test
    @DisplayName("가게 소유자는 리뷰에 답글을 등록할 수 있다")
    void createReply_success() {

        // given
        ReviewReplyRequest request = mock(ReviewReplyRequest.class);

        when(request.getContent()).thenReturn("이용해주셔서 감사합니다.");

        // 삭제되지 않은 리뷰 조회
        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.of(review));

        // 해당 리뷰가 작성된 가게 조회
        when(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                .thenReturn(Optional.of(store));

        // 현재 로그인 사용자가 실제 가게 소유자임
        when(store.getUserId()).thenReturn(ownerId);

        // 기존 답글이 존재하지 않음
        when(reviewReplyRepository.existsByReviewIdAndDeletedAtIsNull(reviewId)).thenReturn(false);

        // 전달받은 답글 엔티티를 그대로 반환
        when(reviewReplyRepository.save(any(ReviewReply.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ReviewReplyResponse response = reviewReplyService.createReply(reviewId, request, ownerId);

        // then
        assertThat(response.getReviewId()).isEqualTo(reviewId);

        assertThat(response.getContent()).isEqualTo("이용해주셔서 감사합니다.");

        verify(reviewRepository).findByIdAndDeletedAtIsNull(reviewId);

        verify(storeRepository).findByStoreIdAndDeletedAtIsNull(storeId);

        verify(reviewReplyRepository).existsByReviewIdAndDeletedAtIsNull(reviewId);

        verify(reviewReplyRepository).save(any(ReviewReply.class));
    }

    @Test
    @DisplayName("이미 답글이 등록된 리뷰에는 답글을 다시 등록할 수 없다")
    void createReply_alreadyExists() {

        // given
        ReviewReplyRequest request = mock(ReviewReplyRequest.class);

        when(request.getContent()).thenReturn("이용해주셔서 감사합니다.");

        // 리뷰 조회 성공
        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.of(review));

        // 가게 조회 성공
        when(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                .thenReturn(Optional.of(store));

        // 현재 로그인 사용자가 실제 가게 소유자임
        when(store.getUserId()).thenReturn(ownerId);

        // 이미 답글이 존재함
        when(reviewReplyRepository.existsByReviewIdAndDeletedAtIsNull(reviewId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> reviewReplyService.createReply(reviewId, request, ownerId))
                .isInstanceOf(ReviewReplyException.class)
                .hasMessage(ReviewReplyErrorCode.REVIEW_REPLY_ALREADY_EXISTS.getMessage());

        verify(reviewRepository).findByIdAndDeletedAtIsNull(reviewId);

        verify(storeRepository).findByStoreIdAndDeletedAtIsNull(storeId);

        verify(reviewReplyRepository).existsByReviewIdAndDeletedAtIsNull(reviewId);

        verify(reviewReplyRepository, never()).save(any(ReviewReply.class));
    }

    @Test
    @DisplayName("가게 소유자가 아닌 사용자는 리뷰에 답글을 등록할 수 없다")
    void createReply_storeOwnerMismatch() {

        // given
        Long anotherOwnerId = 2L;

        ReviewReplyRequest request = mock(ReviewReplyRequest.class);

        when(request.getContent()).thenReturn("답글을 등록하려고 합니다.");

        // 삭제되지 않은 리뷰 조회 성공
        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId)).thenReturn(Optional.of(review));

        // 리뷰가 작성된 가게 조회 성공
        when(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                .thenReturn(Optional.of(store));

        // 실제 가게 소유자는 ownerId이고, 요청 사용자는 anotherOwnerId
        when(store.getUserId()).thenReturn(ownerId);

        // when & then
        assertThatThrownBy(() -> reviewReplyService.createReply(reviewId, request, anotherOwnerId))
                .isInstanceOf(ReviewReplyException.class)
                .hasMessage(ReviewReplyErrorCode.REVIEW_REPLY_ACCESS_DENIED.getMessage());

        // 리뷰와 가게 조회까지만 수행
        verify(reviewRepository).findByIdAndDeletedAtIsNull(reviewId);

        verify(storeRepository).findByStoreIdAndDeletedAtIsNull(storeId);

        // 소유자 검증에서 실패하므로 중복 확인과 저장은 수행되지 않음
        verifyNoInteractions(reviewReplyRepository);
    }

    @Test
    @DisplayName("답글 작성자는 자신의 답글을 수정할 수 있다")
    void updateReply_success() {

        // given
        UUID replyId = UUID.randomUUID();

        ReviewReply reply = new ReviewReply(review, "기존 답글 내용", ownerId);

        // JPA가 생성하는 답글 ID를 테스트에서 직접 설정
        ReflectionTestUtils.setField(reply, "id", replyId);

        ReviewReplyRequest request = mock(ReviewReplyRequest.class);

        when(request.getContent()).thenReturn("수정된 답글 내용");

        // 삭제되지 않은 답글 조회 성공
        when(reviewReplyRepository.findByIdAndDeletedAtIsNull(replyId))
                .thenReturn(Optional.of(reply));

        // when
        ReviewReplyResponse response =
                reviewReplyService.updateReply(reviewId, replyId, request, ownerId);

        // then
        assertThat(response.getReviewReplyId()).isEqualTo(replyId);
        assertThat(response.getReviewId()).isEqualTo(reviewId);
        assertThat(response.getContent()).isEqualTo("수정된 답글 내용");

        // 답글 조회 여부 확인
        verify(reviewReplyRepository).findByIdAndDeletedAtIsNull(replyId);

        // 수정은 Dirty Checking으로 처리하므로 save()는 호출하지 않음
        verify(reviewReplyRepository, never()).save(any(ReviewReply.class));
    }

    @Test
    @DisplayName("답글 작성자는 자신의 답글을 삭제할 수 있다")
    void deleteReply_success() {

        // given
        UUID replyId = UUID.randomUUID();

        ReviewReply reply = new ReviewReply(review, "삭제할 답글 내용", ownerId);

        // JPA가 생성하는 답글 ID를 테스트에서 직접 설정
        ReflectionTestUtils.setField(reply, "id", replyId);

        when(reviewReplyRepository.findByIdAndDeletedAtIsNull(replyId))
                .thenReturn(Optional.of(reply));

        // when
        reviewReplyService.deleteReply(reviewId, replyId, ownerId, ownerId.toString());

        // then
        assertThat(reply.getDeletedAt()).isNotNull();
        assertThat(reply.getDeletedBy()).isEqualTo(ownerId.toString());

        verify(reviewReplyRepository).findByIdAndDeletedAtIsNull(replyId);
    }
}
