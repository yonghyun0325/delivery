package com.delivery.domain.reviewreply.dto.response;

import com.delivery.domain.reviewreply.entity.ReviewReply;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewReplyResponse {

    // 답글 ID
    private UUID reviewReplyId;

    // 리뷰 ID
    private UUID reviewId;

    // 사장님 답글 내용
    private String content;

    // 답글 상태
    private String status;

    // 생성일
    private LocalDateTime createdAt;

    // 생성자
    private String createdBy;

    public static ReviewReplyResponse toDto(ReviewReply reply) {
        return ReviewReplyResponse.builder()
                .reviewReplyId(reply.getId())
                .reviewId(reply.getReview().getId())
                .content(reply.getContent())
                .status(reply.getStatus())
                .createdAt(reply.getCreatedAt())
                .createdBy(reply.getCreatedBy())
                .build();
    }
}
