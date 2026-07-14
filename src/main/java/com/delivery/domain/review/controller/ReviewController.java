package com.delivery.domain.review.controller;

import com.delivery.domain.review.dto.request.ReviewRequest;
import com.delivery.domain.review.dto.response.ReviewResponse;
import com.delivery.domain.review.service.ReviewService;
import com.delivery.global.security.config.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    // 로그인한 고객만 리뷰 등록
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/reviews")
    public ReviewResponse createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewRequest request) {

        return reviewService.createReview(userDetails.getId(), request);
    }

    // 로그인 사용자만 리뷰 상세 조회
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/reviews/{reviewId}")
    public ReviewResponse getReview(@PathVariable UUID reviewId) {
        return reviewService.getReview(reviewId);
    }

    // 작성자 본인만 수정
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/reviews/{reviewId}")
    public ReviewResponse updateReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewRequest request) {

        return reviewService.updateReview(reviewId, userDetails.getId(), request);
    }

    // 작성자 본인만 삭제
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/reviews/{reviewId}")
    public void deleteReview(
            @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable UUID reviewId) {

        reviewService.deleteReview(reviewId, userDetails.getId());
    }

    // 음식점 리뷰 목록 조회
    @GetMapping("/stores/{storeId}/reviews")
    public List<ReviewResponse> getReviewsByStore(@PathVariable UUID storeId) {
        return reviewService.getReviewsByStore(storeId);
    }

    // 로그인한 사용자의 리뷰 목록 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/reviews/me")
    public List<ReviewResponse> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return reviewService.getMyReviews(userDetails.getId());
    }

    // 음식점 평균 평점 조회
    @GetMapping("/stores/{storeId}/ratings")
    public Double getStoreRating(@PathVariable UUID storeId) {
        return reviewService.getStoreRating(storeId);
    }

    // 메뉴 평균 평점 조회
    @GetMapping("/menus/{menuId}/ratings")
    public Double getMenuRating(@PathVariable UUID menuId) {
        return reviewService.getMenuRating(menuId);
    }
}
