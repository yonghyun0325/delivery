package com.delivery.domain.review.service;

import com.delivery.domain.review.dto.request.ReviewRequest;
import com.delivery.domain.review.dto.response.ReviewResponse;
import com.delivery.domain.review.entity.Review;
import com.delivery.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {

        // 요청 DTO를 Review Entity로 변환
        Review review =
                Review.create(
                        request.getOrderId(),
                        request.getUserId(),
                        request.getStoreId(),
                        request.getRating(),
                        request.getContent().toString());

        // 리뷰 저장
        Review savedReview = reviewRepository.save(review);

        // 저장된 Review Entity를 ReviewResponse DTO로 변환하여 반환
        return new ReviewResponse(savedReview);
    }
}
