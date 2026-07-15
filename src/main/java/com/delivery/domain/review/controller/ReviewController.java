package com.delivery.domain.review.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.review.dto.request.ReviewRequest;
import com.delivery.domain.review.dto.response.ReviewResponse;
import com.delivery.domain.review.enums.ReviewSortType;
import com.delivery.domain.review.service.ReviewService;
import com.delivery.global.security.config.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewController implements ReviewApi {

    private final ReviewService reviewService;

    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/reviews")
    public ResponseEntity<RestApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewRequest request) {

        ReviewResponse response = reviewService.createReview(userDetails.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestApiResponse.success(HttpStatus.CREATED, "리뷰 등록에 성공했습니다.", response));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<RestApiResponse<ReviewResponse>> getReview(@PathVariable UUID reviewId) {

        ReviewResponse response = reviewService.getReview(reviewId);

        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "리뷰 상세 조회에 성공했습니다.", response));
    }

    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<RestApiResponse<ReviewResponse>> updateReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewRequest request) {

        ReviewResponse response =
                reviewService.updateReview(reviewId, userDetails.getId(), request);

        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "리뷰 수정에 성공했습니다.", response));
    }

    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<RestApiResponse<Void>> deleteReview(
            @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable UUID reviewId) {

        reviewService.deleteReview(reviewId, userDetails.getId());

        return ResponseEntity.ok(RestApiResponse.success(HttpStatus.OK, "리뷰 삭제에 성공했습니다.", null));
    }

    @Override
    @GetMapping("/stores/{storeId}/reviews")
    public ResponseEntity<RestApiResponse<Page<ReviewResponse>>> getReviewsByStore(
            @PathVariable UUID storeId,
            @RequestParam(defaultValue = "LATEST") ReviewSortType sortType,
            Pageable pageable) {

        Page<ReviewResponse> response =
                reviewService.getReviewsByStore(storeId, sortType, pageable);

        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "음식점 리뷰 목록 조회에 성공했습니다.", response));
    }

    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/reviews/me")
    public ResponseEntity<RestApiResponse<List<ReviewResponse>>> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<ReviewResponse> response = reviewService.getMyReviews(userDetails.getId());

        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "내 리뷰 목록 조회에 성공했습니다.", response));
    }

    @Override
    @GetMapping("/stores/{storeId}/ratings")
    public ResponseEntity<RestApiResponse<Double>> getStoreRating(@PathVariable UUID storeId) {

        Double response = reviewService.getStoreRating(storeId);

        return ResponseEntity.ok(
                RestApiResponse.success(HttpStatus.OK, "음식점 평균 평점 조회에 성공했습니다.", response));
    }
}
