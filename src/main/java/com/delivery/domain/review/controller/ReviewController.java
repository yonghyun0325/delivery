package com.delivery.domain.review.controller;

import com.delivery.domain.review.dto.request.ReviewRequest;
import com.delivery.domain.review.dto.response.ReviewResponse;
import com.delivery.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(@Valid @RequestBody ReviewRequest request) {

        System.out.println("리뷰 등록!");

        // 리뷰 등록 서비스 호출
        return reviewService.createReview(request);
    }
}
