package com.delivery.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.delivery.domain.order.entity.Order;
import com.delivery.domain.order.enums.OrderStatus;
import com.delivery.domain.order.repository.OrderRepository;
import com.delivery.domain.review.dto.request.ReviewRequest;
import com.delivery.domain.review.dto.response.ReviewResponse;
import com.delivery.domain.review.entity.Review;
import com.delivery.domain.review.exception.ReviewErrorCode;
import com.delivery.domain.review.exception.ReviewException;
import com.delivery.domain.review.repository.ReviewRepository;
import com.delivery.domain.store.service.StoreService;
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
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private StoreService storeService;

    @InjectMocks
    private ReviewService reviewService;

    private Long loginUserId;
    private UUID orderId;
    private UUID storeId;
    private Order completedOrder;

    @BeforeEach
    void setUp() {

        loginUserId = 1L;
        orderId = UUID.randomUUID();
        storeId = UUID.randomUUID();

        // 리뷰 작성 대상 주문 생성
        completedOrder =
                new Order(
                        loginUserId,
                        storeId,
                        "서울특별시 강남구");

        // JPA가 생성하는 주문 ID를 단위 테스트에서 직접 설정
        ReflectionTestUtils.setField(
                completedOrder,
                "id",
                orderId);

        // 리뷰를 작성할 수 있는 최종 완료 상태로 변경
        completedOrder.changeStatus(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("완료된 본인 주문에 리뷰를 등록할 수 있다")
    void createReview_success() {

        // given
        ReviewRequest request = mock(ReviewRequest.class);

        when(request.getOrderId()).thenReturn(orderId);
        when(request.getRating()).thenReturn(5);
        when(request.getContent()).thenReturn("정말 맛있었습니다!");

        // 삭제되지 않은 주문 조회 성공
        when(orderRepository.findByIdAndDeletedAtIsNull(orderId))
                .thenReturn(Optional.of(completedOrder));

        // 해당 주문에 작성된 리뷰가 없음
        when(reviewRepository.existsByOrderId(orderId))
                .thenReturn(false);

        // 전달받은 Review 엔티티를 그대로 반환
        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ReviewResponse response =
                reviewService.createReview(loginUserId, request);

        // then
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getContent()).isEqualTo("정말 맛있었습니다!");
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getUserId()).isEqualTo(loginUserId);
        assertThat(response.getStoreId()).isEqualTo(storeId);

        // 주문 조회 여부 검증
        verify(orderRepository)
                .findByIdAndDeletedAtIsNull(orderId);

        // 주문당 리뷰 중복 검증 여부 확인
        verify(reviewRepository)
                .existsByOrderId(orderId);

        // 리뷰 저장 여부 확인
        verify(reviewRepository)
                .save(any(Review.class));

        // 가게 평균 평점 갱신 여부 확인
        verify(storeService)
                .updateAverageRating(storeId);
    }

    @Test
    @DisplayName("존재하지 않거나 삭제된 주문에는 리뷰를 등록할 수 없다")
    void createReview_orderNotFound() {

        // given
        ReviewRequest request = mock(ReviewRequest.class);

        when(request.getOrderId()).thenReturn(orderId);
        when(request.getRating()).thenReturn(5);
        when(request.getContent()).thenReturn("정말 맛있었습니다!");

        // 삭제되지 않은 주문을 찾지 못한 상황
        when(orderRepository.findByIdAndDeletedAtIsNull(orderId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(
                () -> reviewService.createReview(loginUserId, request))
                .isInstanceOf(ReviewException.class)
                .hasMessage(ReviewErrorCode.ORDER_NOT_FOUND.getMessage());

        // 주문 조회는 수행되어야 함
        verify(orderRepository)
                .findByIdAndDeletedAtIsNull(orderId);

        // 주문이 없으므로 리뷰 관련 로직과 평균 평점 갱신은 실행되지 않음
        verifyNoInteractions(
                reviewRepository,
                storeService);
    }

    @Test
    @DisplayName("주문자가 아닌 사용자는 리뷰를 등록할 수 없다")
    void createReview_orderUserMismatch() {

        // given
        Long anotherUserId = 2L;

        ReviewRequest request = mock(ReviewRequest.class);

        when(request.getOrderId()).thenReturn(orderId);
        when(request.getRating()).thenReturn(5);
        when(request.getContent()).thenReturn("정말 맛있었습니다!");

        // 주문은 존재하지만 로그인 사용자와 주문자가 다름
        when(orderRepository.findByIdAndDeletedAtIsNull(orderId))
                .thenReturn(Optional.of(completedOrder));

        // when & then
        assertThatThrownBy(
                () -> reviewService.createReview(anotherUserId, request))
                .isInstanceOf(ReviewException.class)
                .hasMessage(ReviewErrorCode.ORDER_USER_MISMATCH.getMessage());

        // 주문 조회까지만 실행
        verify(orderRepository)
                .findByIdAndDeletedAtIsNull(orderId);

        // 주문자 검증에서 실패하므로 중복 확인과 저장은 실행되지 않음
        verifyNoInteractions(
                reviewRepository,
                storeService);
    }

    @Test
    @DisplayName("완료되지 않은 주문에는 리뷰를 등록할 수 없다")
    void createReview_orderNotCompleted() {

        // given
        Order pendingOrder =
                new Order(
                        loginUserId,
                        storeId,
                        "서울특별시 강남구");

        // 테스트에서 사용할 주문 ID 직접 설정
        ReflectionTestUtils.setField(
                pendingOrder,
                "id",
                orderId);

        ReviewRequest request = mock(ReviewRequest.class);

        when(request.getOrderId()).thenReturn(orderId);
        when(request.getRating()).thenReturn(5);
        when(request.getContent()).thenReturn("정말 맛있었습니다!");

        // 주문은 존재하지만 완료 상태가 아님
        when(orderRepository.findByIdAndDeletedAtIsNull(orderId))
                .thenReturn(Optional.of(pendingOrder));

        // when & then
        assertThatThrownBy(
                () -> reviewService.createReview(loginUserId, request))
                .isInstanceOf(ReviewException.class)
                .hasMessage(ReviewErrorCode.ORDER_NOT_COMPLETED.getMessage());

        // 주문 조회까지만 실행
        verify(orderRepository)
                .findByIdAndDeletedAtIsNull(orderId);

        // 주문 상태 검증에서 실패하므로 이후 로직은 실행되지 않음
        verifyNoInteractions(
                reviewRepository,
                storeService);
    }

    @Test
    @DisplayName("이미 리뷰가 등록된 주문에는 리뷰를 다시 등록할 수 없다")
    void createReview_reviewAlreadyExists() {

        // given
        ReviewRequest request = mock(ReviewRequest.class);

        when(request.getOrderId()).thenReturn(orderId);
        when(request.getRating()).thenReturn(5);
        when(request.getContent()).thenReturn("정말 맛있었습니다!");

        // 삭제되지 않은 완료 주문 조회
        when(orderRepository.findByIdAndDeletedAtIsNull(orderId))
                .thenReturn(Optional.of(completedOrder));

        // 해당 주문에 이미 리뷰가 존재함
        when(reviewRepository.existsByOrderId(orderId))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(
                () -> reviewService.createReview(loginUserId, request))
                .isInstanceOf(ReviewException.class)
                .hasMessage(ReviewErrorCode.REVIEW_ALREADY_EXISTS.getMessage());

        // 주문 조회와 중복 리뷰 검증까지 수행
        verify(orderRepository)
                .findByIdAndDeletedAtIsNull(orderId);

        verify(reviewRepository)
                .existsByOrderId(orderId);

        // 중복 리뷰이므로 저장은 실행되지 않음
        verify(reviewRepository, never())
                .save(any(Review.class));

        // 평균 평점 갱신도 실행되지 않음
        verifyNoInteractions(storeService);
    }

    @Test
    @DisplayName("평점이 1점 미만이면 리뷰를 등록할 수 없다")
    void createReview_ratingBelowMinimum() {

        // given
        ReviewRequest request = mock(ReviewRequest.class);

        when(request.getRating()).thenReturn(0);

        // when & then
        assertThatThrownBy(
                () -> reviewService.createReview(loginUserId, request))
                .isInstanceOf(ReviewException.class)
                .hasMessage(ReviewErrorCode.INVALID_RATING.getMessage());

        // 요청 데이터 검증에서 실패하므로 이후 로직은 실행되지 않음
        verifyNoInteractions(
                orderRepository,
                reviewRepository,
                storeService);
    }

    @Test
    @DisplayName("평점이 5점을 초과하면 리뷰를 등록할 수 없다")
    void createReview_ratingAboveMaximum() {

        // given
        ReviewRequest request = mock(ReviewRequest.class);

        when(request.getRating()).thenReturn(6);

        // when & then
        assertThatThrownBy(
                () -> reviewService.createReview(loginUserId, request))
                .isInstanceOf(ReviewException.class)
                .hasMessage(ReviewErrorCode.INVALID_RATING.getMessage());

        // 요청 데이터 검증에서 실패하므로 이후 로직은 실행되지 않음
        verifyNoInteractions(
                orderRepository,
                reviewRepository,
                storeService);
    }

    @Test
    @DisplayName("평점이 없으면 리뷰를 등록할 수 없다")
    void createReview_ratingIsNull() {

        // given
        ReviewRequest request = mock(ReviewRequest.class);

        when(request.getRating()).thenReturn(null);

        // when & then
        assertThatThrownBy(
                () -> reviewService.createReview(loginUserId, request))
                .isInstanceOf(ReviewException.class)
                .hasMessage(ReviewErrorCode.INVALID_RATING.getMessage());

        // 평점 검증에서 바로 실패하므로 이후 로직은 실행되지 않음
        verifyNoInteractions(
                orderRepository,
                reviewRepository,
                storeService);
    }

    @Test
    @DisplayName("리뷰 작성자는 자신의 리뷰를 수정할 수 있다")
    void updateReview_success() {

        // given
        UUID reviewId = UUID.randomUUID();

        Review review =
                Review.create(
                        orderId,
                        loginUserId,
                        storeId,
                        3,
                        "기존 리뷰 내용");

        // JPA가 생성하는 리뷰 ID를 단위 테스트에서 직접 설정
        ReflectionTestUtils.setField(
                review,
                "id",
                reviewId);

        ReviewRequest request = mock(ReviewRequest.class);

        when(request.getRating()).thenReturn(5);
        when(request.getContent()).thenReturn("수정된 리뷰 내용");

        // 삭제되지 않은 리뷰 조회 성공
        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        // when
        ReviewResponse response =
                reviewService.updateReview(
                        reviewId,
                        loginUserId,
                        request);

        // then
        assertThat(response.getReviewId()).isEqualTo(reviewId);
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getContent()).isEqualTo("수정된 리뷰 내용");
        assertThat(response.getUserId()).isEqualTo(loginUserId);
        assertThat(response.getStoreId()).isEqualTo(storeId);

        // 삭제되지 않은 리뷰 조회 여부 확인
        verify(reviewRepository)
                .findByIdAndDeletedAtIsNull(reviewId);

        // 수정 후 가게 평균 평점 갱신 여부 확인
        verify(storeService)
                .updateAverageRating(storeId);
    }

    @Test
    @DisplayName("존재하지 않거나 삭제된 리뷰는 수정할 수 없다")
    void updateReview_reviewNotFound() {

        // given
        UUID reviewId = UUID.randomUUID();

        ReviewRequest request = mock(ReviewRequest.class);

        when(request.getRating()).thenReturn(5);
        when(request.getContent()).thenReturn("수정된 리뷰 내용");

        // 삭제되지 않은 리뷰를 찾지 못한 상황
        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(
                () -> reviewService.updateReview(reviewId, loginUserId, request))
                .isInstanceOf(ReviewException.class)
                .hasMessage(ReviewErrorCode.REVIEW_NOT_FOUND.getMessage());

        // 리뷰 조회는 수행되어야 함
        verify(reviewRepository)
                .findByIdAndDeletedAtIsNull(reviewId);

        // 리뷰를 찾지 못했으므로 저장 및 평균 평점 갱신은 실행되지 않음
        verify(reviewRepository, never())
                .save(any(Review.class));

        verifyNoInteractions(storeService);
    }

    @Test
    @DisplayName("다른 사용자가 작성한 리뷰는 수정할 수 없다")
    void updateReview_accessDenied() {

        // given
        UUID reviewId = UUID.randomUUID();
        Long anotherUserId = 2L;

        Review review =
                Review.create(
                        orderId,
                        loginUserId,
                        storeId,
                        3,
                        "기존 리뷰 내용");

        ReflectionTestUtils.setField(
                review,
                "id",
                reviewId);

        ReviewRequest request = mock(ReviewRequest.class);

        when(request.getRating()).thenReturn(5);
        when(request.getContent()).thenReturn("수정된 리뷰 내용");

        // 리뷰는 존재하지만 로그인 사용자가 작성자가 아님
        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        // when & then
        assertThatThrownBy(
                () -> reviewService.updateReview(reviewId, anotherUserId, request))
                .isInstanceOf(ReviewException.class)
                .hasMessage(ReviewErrorCode.REVIEW_ACCESS_DENIED.getMessage());

        // 리뷰 조회까지만 수행
        verify(reviewRepository)
                .findByIdAndDeletedAtIsNull(reviewId);

        // 권한 검증에서 실패하므로 저장과 평균 평점 갱신은 실행되지 않음
        verify(reviewRepository, never())
                .save(any(Review.class));

        verifyNoInteractions(storeService);
    }

    @Test
    @DisplayName("리뷰 작성자는 자신의 리뷰를 삭제할 수 있다")
    void deleteReview_success() {

        // given
        UUID reviewId = UUID.randomUUID();

        Review review =
                Review.create(
                        orderId,
                        loginUserId,
                        storeId,
                        5,
                        "삭제할 리뷰 내용");

        ReflectionTestUtils.setField(
                review,
                "id",
                reviewId);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        // when
        reviewService.deleteReview(reviewId, loginUserId);

        // then
        assertThat(review.getDeletedAt()).isNotNull();
        assertThat(review.getDeletedBy()).isEqualTo(loginUserId.toString());

        verify(reviewRepository)
                .findByIdAndDeletedAtIsNull(reviewId);

        verify(storeService)
                .updateAverageRating(storeId);
    }

    @Test
    @DisplayName("다른 사용자가 작성한 리뷰는 삭제할 수 없다")
    void deleteReview_accessDenied() {

        // given
        UUID reviewId = UUID.randomUUID();
        Long anotherUserId = 2L;

        Review review =
                Review.create(
                        orderId,
                        loginUserId,
                        storeId,
                        5,
                        "삭제 권한 테스트 리뷰");

        ReflectionTestUtils.setField(
                review,
                "id",
                reviewId);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        // when & then
        assertThatThrownBy(
                () -> reviewService.deleteReview(reviewId, anotherUserId))
                .isInstanceOf(ReviewException.class)
                .hasMessage(ReviewErrorCode.REVIEW_ACCESS_DENIED.getMessage());

        // 리뷰 조회는 수행됨
        verify(reviewRepository)
                .findByIdAndDeletedAtIsNull(reviewId);

        // 권한 검증에서 실패하므로 평균 평점 갱신은 실행되지 않음
        verifyNoInteractions(storeService);
    }
}