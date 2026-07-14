package com.delivery.domain.reviewreply.controller;

import com.delivery.common.RestApiResponse;
import com.delivery.domain.reviewreply.dto.request.ReviewReplyRequest;
import com.delivery.domain.reviewreply.dto.response.ReviewReplyResponse;
import com.delivery.domain.reviewreply.service.ReviewReplyService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews/{reviewId}/replies")
@RequiredArgsConstructor
public class ReviewReplyController {

    private final ReviewReplyService reviewReplyService;

    // 사장님 리뷰 답글 등록
    @PostMapping
    public ResponseEntity<RestApiResponse<ReviewReplyResponse>> createReply(
            @PathVariable UUID reviewId, @Valid @RequestBody ReviewReplyRequest request) {

        // TODO: Security 적용 후 로그인 사용자 ID로 교체 예정
        UUID ownerId = UUID.randomUUID();

        ReviewReplyResponse response = reviewReplyService.createReply(reviewId, request, ownerId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestApiResponse.success(HttpStatus.CREATED, "사장님 리뷰 답글 등록 성공", response));
    }

    // 사장님 리뷰 답글 조회
    @GetMapping
    public ResponseEntity<RestApiResponse<ReviewReplyResponse>> getReply(
            @PathVariable UUID reviewId) {

        ReviewReplyResponse response = reviewReplyService.getReply(reviewId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(RestApiResponse.success(HttpStatus.OK, "사장님 리뷰 답글 조회 성공", response));
    }

    // 사장님 리뷰 답글 수정
    @PatchMapping("/{replyId}")
    public ResponseEntity<RestApiResponse<ReviewReplyResponse>> updateReply(
            @PathVariable UUID reviewId,
            @PathVariable UUID replyId,
            @Valid @RequestBody ReviewReplyRequest request) {

        ReviewReplyResponse response = reviewReplyService.updateReply(reviewId, replyId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(RestApiResponse.success(HttpStatus.OK, "사장님 리뷰 답글 수정 성공", response));
    }

    // 사장님 리뷰 답글 삭제
    @DeleteMapping("/{replyId}")
    public ResponseEntity<RestApiResponse<Void>> deleteReply(
            @PathVariable UUID reviewId, @PathVariable UUID replyId) {

        // TODO: Security 적용 후 로그인 사용자 username으로 교체 예정
        String deletedBy = "owner";

        reviewReplyService.deleteReply(reviewId, replyId, deletedBy);

        return ResponseEntity.status(HttpStatus.OK)
                .body(RestApiResponse.success(HttpStatus.OK, "사장님 리뷰 답글 삭제 성공", null));
    }
}
