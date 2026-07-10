package com.delivery.domain.review.dto.response;

import com.delivery.domain.review.entity.Review;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ReviewResponse {

    // 리뷰 ID
    private UUID reviewId;
    // 주문 ID
    private UUID orderId;
    // 리뷰 작성자 ID
    private Long userId;
    // 가게 ID
    private UUID storeId;
    // 평점
    private Integer rating;
    // 리뷰 내용
    private String content;
    // 작성자
    private String createdBy;
    // 작성일
    private LocalDateTime createdAt;

    public ReviewResponse(Review review) {
        this.reviewId = review.getId();
        this.orderId = review.getOrderId();
        this.userId = review.getUserId();
        this.storeId = review.getStoreId();
        this.rating = review.getRating();
        this.content = review.getContent();
        this.createdBy = review.getCreatedBy();
        this.createdAt = review.getCreatedAt();
    }

    /** Entity를 Response DTO로 변환 */
    public static ReviewResponse toDto(Review Review) {
        return new ReviewResponse(Review);
    }
}
