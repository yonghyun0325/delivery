package com.delivery.domain.review.service;

import com.delivery.domain.review.dto.request.ReviewRequest;
import com.delivery.domain.review.dto.response.ReviewResponse;
import com.delivery.domain.review.entity.Review;
import com.delivery.domain.review.exception.ReviewErrorCode;
import com.delivery.domain.review.exception.ReviewException;
import com.delivery.domain.review.repository.ReviewRepository;
import com.delivery.domain.store.service.StoreService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StoreService storeService;

    // 리뷰 등록
    @Transactional
    public ReviewResponse createReview(Long loginUserId, ReviewRequest request) {
        validateReviewRequest(request);

        Review review =
                Review.create(
                        request.getOrderId(),
                        loginUserId,
                        request.getStoreId(),
                        request.getRating(),
                        request.getContent());

        Review savedReview = reviewRepository.save(review);

        storeService.updateAverageRating(savedReview.getStoreId());

        return ReviewResponse.toDto(savedReview);
    }

    // 리뷰 단건 조회
    public ReviewResponse getReview(UUID reviewId) {
        Review review = findActiveReviewById(reviewId);

        return ReviewResponse.toDto(review);
    }

    // 리뷰 전체 조회
    public List<ReviewResponse> getReviews() {
        return reviewRepository.findAllByDeletedAtIsNull().stream()
                .map(ReviewResponse::toDto)
                .toList();
    }

    // 음식점 리뷰 목록 조회
    public List<ReviewResponse> getReviewsByStore(UUID storeId) {
        return reviewRepository.findAllByStoreIdAndDeletedAtIsNull(storeId).stream()
                .map(ReviewResponse::toDto)
                .toList();
    }

    // 로그인한 사용자의 리뷰 목록 조회
    public List<ReviewResponse> getMyReviews(Long loginUserId) {
        return reviewRepository.findAllByUserIdAndDeletedAtIsNull(loginUserId).stream()
                .map(ReviewResponse::toDto)
                .toList();
    }

    // 음식점 평균 평점 조회
    public Double getStoreRating(UUID storeId) {
        Double averageRating = reviewRepository.findAverageRatingByStoreId(storeId);

        return averageRating == null ? 0.0 : averageRating;
    }

    // 메뉴 평균 평점 조회
    public Double getMenuRating(UUID menuId) {
        return 0.0;
    }

    // 리뷰 수정
    @Transactional
    public ReviewResponse updateReview(
            UUID reviewId,
            Long loginUserId,
            ReviewRequest request) {

        validateReviewRequest(request);

        Review review = findActiveReviewById(reviewId);

        validateReviewOwner(review, loginUserId);

        review.update(request.getRating(), request.getContent());

        storeService.updateAverageRating(review.getStoreId());

        return ReviewResponse.toDto(review);
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(UUID reviewId, Long loginUserId) {
        Review review = findActiveReviewById(reviewId);

        validateReviewOwner(review, loginUserId);

        review.delete(loginUserId.toString());

        storeService.updateAverageRating(review.getStoreId());
    }

    // 가게 삭제 시 해당 가게의 리뷰 전체 삭제
    @Transactional
    public void deleteReviewsByStoreId(UUID storeId, String deletedBy) {
        List<Review> reviews =
                reviewRepository.findAllByStoreIdAndDeletedAtIsNull(storeId);

        reviews.forEach(review -> review.delete(deletedBy));
    }

    // 삭제되지 않은 리뷰 조회
    private Review findActiveReviewById(UUID reviewId) {
        return reviewRepository
                .findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

    // 리뷰 작성자 검증
    private void validateReviewOwner(Review review, Long loginUserId) {
        if (!review.getUserId().equals(loginUserId)) {
            throw new ReviewException(ReviewErrorCode.REVIEW_ACCESS_DENIED);
        }
    }

    // 리뷰 요청 데이터 검증
    private void validateReviewRequest(ReviewRequest request) {
        if (request.getRating() == null
                || request.getRating() < 1
                || request.getRating() > 5) {
            throw new ReviewException(ReviewErrorCode.INVALID_RATING);
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new ReviewException(ReviewErrorCode.EMPTY_CONTENT);
        }
    }
}