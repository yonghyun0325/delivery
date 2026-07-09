package com.delivery.domain.review.controller;

import com.delivery.domain.review.dto.request.ReviewRequest;
import com.delivery.domain.review.dto.response.ReviewResponse;
import com.delivery.domain.review.service.ReviewService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    // 고객 리뷰 생성
    @PostMapping("/reviews")
    public ReviewResponse createReview(@RequestBody ReviewRequest request) {
        return reviewService.createReview(request);
    }

    // 리뷰 상세 조회
    @GetMapping("/reviews/{reviewId}")
    public ReviewResponse getReview(@PathVariable UUID reviewId) {
        return reviewService.getReview(reviewId);
    }

    // 리뷰 수정
    @PatchMapping("/reviews/{reviewId}")
    public ReviewResponse updateReview(
            @PathVariable UUID reviewId, @RequestBody ReviewRequest request) {
        return reviewService.updateReview(reviewId, request);
    }

    // 리뷰 삭제
    @DeleteMapping("/reviews/{reviewId}")
    public void deleteReview(@PathVariable UUID reviewId) {
        reviewService.deleteReview(reviewId, "SYSTEM");
    }

    // 음식점 리뷰 목록 조회
    @GetMapping("/store/{storeId}/reviews")
    public List<ReviewResponse> getReviewsByStore(@PathVariable UUID storeId) {
        return reviewService.getReviewsByStore(storeId);
    }

    // 내 리뷰 목록 조회
    @GetMapping("/reviews/me")
    public List<ReviewResponse> getMyReviews() {

        UUID userId = UUID.fromString("000000000-0000-0000-0000-000000000001");

        return reviewService.getMyReviews(userId);
    }

    // 음식점 평점 조회
    @GetMapping("/store/{storeId}/ratings")
    public Double getStoreRating(@PathVariable UUID storeId) {
        return reviewService.getStoreRating(storeId);
    }

    // 메뉴 평점 조회
    @GetMapping("/menu/{menuId}/ratings")
    public Double getMenuRating(@PathVariable UUID menuId) {
        return reviewService.getMenuRating(menuId);
    }
}
