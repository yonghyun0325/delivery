package com.delivery.domain.review.service;

import com.delivery.domain.review.dto.request.ReviewRequest;
import com.delivery.domain.review.dto.response.ReviewResponse;
import com.delivery.domain.review.entity.Review;
import com.delivery.domain.review.repository.ReviewRepository;
import com.delivery.global.exception.ReviewErrorCode;
import com.delivery.global.exception.ReviewException;
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

    // 리뷰 등록
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        validateReviewRequest(request);

        Review review =
                Review.create(
                        request.getOrderId(),
                        request.getUserId(),
                        request.getStoreId(),
                        request.getRating(),
                        request.getContent());

        Review savedReview = reviewRepository.save(review);

        return ReviewResponse.toDto(savedReview);
    }

    // 리뷰 단건 조회
    public ReviewResponse getReview(UUID reviewId) {
        Review review = findReviewById(reviewId);

        return ReviewResponse.toDto(review);
    }

    // 리뷰 전체 조회
    public List<ReviewResponse> getReviews() {
        return reviewRepository.findAll().stream().map(ReviewResponse::toDto).toList();
    }

    // 음식점 리뷰 목록 조회
    public List<ReviewResponse> getReviewsByStore(UUID storeId) {
        return reviewRepository.findAllByStoreId(storeId).stream()
                .map(ReviewResponse::toDto)
                .toList();
    }

    // 내 리뷰 목록 조회
    public List<ReviewResponse> getMyReviews(UUID userId) {
        return reviewRepository.findAllByUserId(userId).stream()
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
    public ReviewResponse updateReview(UUID reviewId, ReviewRequest request) {
        validateReviewRequest(request);

        Review review = findReviewById(reviewId);

        review.update(request.getRating(), request.getContent());

        return ReviewResponse.toDto(review);
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(UUID reviewId, String deletedBy) {
        Review review = findReviewById(reviewId);

        review.delete(deletedBy);
    }

    // 리뷰 조회 공통 메서드
    private Review findReviewById(UUID reviewId) {
        return reviewRepository
                .findById(reviewId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

    // 리뷰 요청 데이터 검증
    private void validateReviewRequest(ReviewRequest request) {
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new ReviewException(ReviewErrorCode.INVALID_RATING);
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new ReviewException(ReviewErrorCode.EMPTY_CONTENT);
        }
    }
}
